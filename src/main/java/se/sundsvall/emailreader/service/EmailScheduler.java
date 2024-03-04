package se.sundsvall.emailreader.service;


import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import microsoft.exchange.webservices.data.property.complex.ItemId;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.mapper.EmailMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@Component
public class EmailScheduler {

	private static final String EMAIL_SUBJECT = "[Warning] EmailReader has detected unhandled emails";

	private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

	private final EWSIntegration ewsIntegration;

	private final MessagingIntegration messagingIntegration;

	private final EmailRepository emailRepository;

	private final CredentialsRepository credentialsRepository;

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
	@SchedulerLock(name = "checkForNewEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	public void checkForNewEmails() {
		log.info("Checking for new emails");

		credentialsRepository.findAll().forEach(credential -> credential.getEmailAddress().forEach(emailAddress -> {

			final var result = ewsIntegration
				.pageThroughEntireInbox(credential.getUsername(), encryptionUtility.decrypt(credential.getPassword()), credential.getDomain(), emailAddress);

			handleNewEmails(result, emailAddress, credential);
		}));
	}

	void handleNewEmails(final List<Email> emails, final String emailAddress, final CredentialsEntity credential){
		for(var email : emails){
			var newEmail = new EmailEntity();
			try {
				newEmail = emailRepository.save(EmailMapper.toEmailEntity(email, credential.getNamespace(), credential.getMunicipalityId(), credential.getMetadata()));
				if(newEmail.getId() != null){
					ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.id()), emailAddress, credential.getDestinationFolder());
				}
			} catch (final Exception e) {
				log.error("Failed to save email", e);
				emailRepository.deleteById(newEmail.getId());
				return;
			}
		}
	}

	@Scheduled(initialDelayString = "${scheduled.email-age-check.initial-delay}", fixedRateString = "${scheduled.email-age-check.fixed-rate}")
	@SchedulerLock(name = "checkForOldEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
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
