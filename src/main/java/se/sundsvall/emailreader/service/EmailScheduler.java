package se.sundsvall.emailreader.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.service.mapper.EmailMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import microsoft.exchange.webservices.data.property.complex.ItemId;

@Component
@EnableScheduling
public class EmailScheduler {

    private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

    private final EWSIntegration ewsIntegration;

    private final EmailRepository emailRepository;

    private final CredentialsRepository credentialsRepository;

    private final EmailMapper emailMapper = new EmailMapper();

    private final EncryptionUtility encryptionUtility;

    public EmailScheduler(final EWSIntegration ewsIntegration, final EmailRepository emailRepository,
        final CredentialsRepository credentialsRepository, final EncryptionUtility encryptionUtility) {

        this.ewsIntegration = ewsIntegration;
        this.emailRepository = emailRepository;
        this.credentialsRepository = credentialsRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Scheduled(initialDelayString = "${scheduled.initial-delay}", fixedRateString = "${scheduled.fixed-rate}")
    public void scheduleEmailReader() {

        log.info("Starting scheduled email reader");

        credentialsRepository.findAll().forEach(credential -> credential.getEmailAdress().forEach(emailAdress -> {

            final var result = ewsIntegration
                .pageThroughEntireInbox(credential.getUsername(), encryptionUtility.decrypt(credential.getPassword()), credential.getDomain(), emailAdress);

            log.info("Found {} emails for mailbox {}", result.size(), emailAdress);

            result.forEach(email -> {

                try {
                    emailRepository.save(emailMapper.toEmailEntity(email, credential.getNamespace(), credential.getMunicipalityId()));
                } catch (final Exception e) {
                    log.error("Failed to save email", e);
                    return;
                }

                try {
                    ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.id()), emailAdress, credential.getDestinationFolder());
                } catch (final Exception e) {
                    log.error("Failed to move email", e);
                }
            });
        }));
        log.info("Finished scheduled email reader");
    }

}
