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
        final var result = credentialsRepository.findAll();
        
        result.forEach(credential -> {

            final var resulty = ewsIntegration
                .pageThroughEntireInbox(credential.getDestinationFolder(),
                    credential.getUsername(), encryptionUtility.decrypt(credential.getPassword()), credential.getDomain());

            log.info("Found {} emails for mailbox {}", resulty.size(), credential.getUsername());

            emailRepository.saveAll(emailMapper.toEmailEntites(resulty, credential.getNamespace(), credential.getMunicipalityId()));

        });

        log.info("Finished scheduled email reader");
    }

}
