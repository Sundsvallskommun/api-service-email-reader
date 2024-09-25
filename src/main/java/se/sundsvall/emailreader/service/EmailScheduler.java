package se.sundsvall.emailreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class EmailScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EmailScheduler.class);

	private final EmailService emailService;

	public EmailScheduler(final EmailService emailService) {
		this.emailService = emailService;
	}

	@Scheduled(cron = "${scheduled.check-for-new-emails.cron}")
	@SchedulerLock(name = "checkForNewEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	public void checkForNewEmails() {
		LOG.info("Checking for new emails");
		for (final var credential : emailService.getAllCredentials()) {
			for (final var address : credential.getEmailAddress()) {
				LOG.info("Fetch mails for address '{}'", address);
				for (final var email : emailService.getAllEmailsInInbox(credential, address)) {
					try {
						emailService.saveAndMoveEmail(email, address, credential);
					} catch (final Exception e) {
						LOG.error("Failed to handle email with subject: {}", email.subject(), e);
					}
				}
			}
		}
	}

	@Scheduled(cron = "${scheduled.check-for-old-emails.cron}")
	@SchedulerLock(name = "checkForOldEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	void checkForOldEmailsAndSendReport() {
		emailService.sendReport();
	}
}
