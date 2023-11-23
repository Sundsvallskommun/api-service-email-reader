package se.sundsvall.emailreader.service;


import java.text.MessageFormat;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.mapper.EmailMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import microsoft.exchange.webservices.data.property.complex.ItemId;

@Component
public class EmailScheduler {

	private static final String EMAIL_SUBJECT = "[Warning] EmailReader has detected unhandled emails";

	private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

	private final EWSIntegration ewsIntegration;

	private final MessagingIntegration messagingIntegration;

	private final EmailRepository emailRepository;

	private final CredentialsRepository credentialsRepository;

	private final EmailMapper emailMapper = new EmailMapper();

	private final EncryptionUtility encryptionUtility;


	public EmailScheduler(final EWSIntegration ewsIntegration, final EmailRepository emailRepository,
		final CredentialsRepository credentialsRepository, final EncryptionUtility encryptionUtility, final MessagingIntegration messagingIntegration) {

		this.ewsIntegration = ewsIntegration;
		this.emailRepository = emailRepository;
		this.credentialsRepository = credentialsRepository;
		this.encryptionUtility = encryptionUtility;
		this.messagingIntegration = messagingIntegration;
	}

	@Scheduled(initialDelayString = "${scheduled.initial-delay}", fixedRateString = "${scheduled.fixed-rate}")
	public void checkForNewEmails() {
		log.info("Checking for new emails");

		credentialsRepository.findAll().forEach(credential -> credential.getEmailAddress().forEach(emailAddress -> {

			final var result = ewsIntegration
				.pageThroughEntireInbox(credential.getUsername(), encryptionUtility.decrypt(credential.getPassword()), credential.getDomain(), emailAddress);


			result.forEach(email -> {

				try {
					emailRepository.save(emailMapper.toEmailEntity(email, credential.getNamespace(), credential.getMunicipalityId(), credential.getMetadata()));
				} catch (final Exception e) {
					log.error("Failed to save email", e);
					return;
				}

				try {
					ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.id()), emailAddress, credential.getDestinationFolder());
				} catch (final Exception e) {
					log.error("Failed to move email", e);
				}
			});
		}));
	}

	@Scheduled(initialDelayString = "${scheduled.email-age-check.initial-delay}", fixedRateString = "${scheduled.email-age-check.fixed-rate}")
	void checkForOldEmails() {

		final var list = emailRepository.findAll().stream()
			.filter(email -> email.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(1)))
			.toList();

		if (!list.isEmpty()) {
			final var message = MessageFormat
				.format("EmailReader has detected unhandled emails with the following IDs: {0}", list.stream()
					.map(EmailEntity::getId)
					.toList());
			messagingIntegration.sendEmail(message, EMAIL_SUBJECT);
		}

	}

}
