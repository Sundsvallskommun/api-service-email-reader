package se.sundsvall.emailreader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.ews.EWSMapper;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;

import generated.se.sundsvall.messaging.SmsRequest;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class EmailScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EmailScheduler.class);

	private final EmailService emailService;
	private final MessagingIntegration messagingIntegration;
	private final EWSIntegration ewsIntegration;

	public EmailScheduler(final EmailService emailService, final MessagingIntegration messagingIntegration, final EWSIntegration ewsIntegration) {
		this.emailService = emailService;
		this.messagingIntegration = messagingIntegration;
		this.ewsIntegration = ewsIntegration;
	}

	@Scheduled(cron = "${scheduled.check-for-new-emails.cron}")
	@SchedulerLock(name = "checkForNewEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	public void checkForNewEmails() {
		LOG.info("Checking for new emails");
		for (final var credential : emailService.findAllByAction("PERSIST")) {
			for (final var address : credential.getEmailAddress()) {
				LOG.info("Fetch mails for address '{}'", address);
				for (final var email : EWSMapper.toEmails(emailService.getAllEmailsInInbox(credential, address))) {
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

	@Scheduled(cron = "${scheduled.check-for-new-sms-emails.cron}")
	@SchedulerLock(name = "checkForNewSmsEmails", lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}")
	void checkForNewSmsEmails() throws Exception {
		LOG.info("Checking for new sms emails");
		var credentials = emailService.findAllByAction("SEND_SMS");
		for (var credential : credentials) {
			for (var address : credential.getEmailAddress()) {
				LOG.info("Fetch sms emails for address '{}'", address);
				for (var emailMessage : emailService.getAllEmailsInInbox(credential, address)) {
					try {
						var emailMap = ewsIntegration.extractValuesEmailMessage(emailMessage);
						var result = ewsIntegration.validateRecipientNumbers(emailMap);
						var validNumbers = result.get("VALID");
						var invalidNumbers = result.get("INVALID");

						if (validNumbers != null) {
							for (var validNumber : validNumbers) {
								var smsRequest = new SmsRequest()
									.sender("Sundsvalls Kommun")
									.message(emailMap.get("Message"))
									.mobileNumber(validNumber);
								messagingIntegration.sendSms(credential.getMunicipalityId(), smsRequest);
							}
						}
						if (invalidNumbers != null) {
							var replyBody = """
								Ditt mejl har hanterats av EmailReader.
								SMS har skickats till:
								%s
								
								Det gick inte att skicka SMS till:
								%s
								""".formatted(validNumbers, invalidNumbers);
							emailMessage.reply(new MessageBody(replyBody), true);
						}
						LOG.debug("Moving sms email to folder '{}'", credential.getDestinationFolder());
						ewsIntegration.moveEmail(emailMessage.getId(), address, credential.getDestinationFolder());
					} catch (Exception e) {
						LOG.error("Failed to handle sms email", e);
						LOG.debug("Moving failed sms email to folder '{}'", credential.getDestinationFolder());
						ewsIntegration.moveEmail(emailMessage.getId(), address, credential.getDestinationFolder());
					}
				}
			}
		}
	}
}
