package se.sundsvall.emailreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class EmailScheduler {

	private static final Logger log = LoggerFactory.getLogger(EmailScheduler.class);

	private final EmailService emailService;

	public EmailScheduler(final EmailService emailService) {
		this.emailService = emailService;
	}

	/**
	 * This method is scheduled to run at a fixed rate to check for new emails and save them to the
	 * database. It is also responsible for moving the emails to the processed folder.
	 */
	@Scheduled(initialDelayString = "${scheduled.initial-delay}", fixedRateString = "${scheduled.fixed-rate}")
	@SchedulerLock(name = "checkForNewEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	public void checkForNewEmails() {
		log.info("Checking for new emails");
		for (final var credential : emailService.getAllCredentials()) {
			for (final var address : credential.getEmailAddress()) {
				for (final var email : emailService.getAllEmailsInInbox(credential, address)) {
					try {
						emailService.saveAndMoveEmail(email, address, credential);
					} catch (Exception e) {
						log.error("Failed to handle email with subject: {}", email.subject(), e);
					}
				}
			}
		}
	}

	/**
	 * This method is scheduled to run at a fixed rate to check for old emails and send a report if
	 * any old emails are found.
	 */
	@Scheduled(initialDelayString = "${scheduled.email-age-check.initial-delay}", fixedRateString = "${scheduled.email-age-check.fixed-rate}")
	@SchedulerLock(name = "checkForOldEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	void checkForOldEmailsAndSendReport() {
		emailService.sendReport();
	}
}
