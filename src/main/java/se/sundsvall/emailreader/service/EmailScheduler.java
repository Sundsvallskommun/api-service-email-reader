package se.sundsvall.emailreader.service;

import static java.lang.Math.min;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.ews.EWSMapper;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;

import generated.se.sundsvall.messaging.SmsRequest;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
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
		for (var credential : emailService.findAllByAction("SEND_SMS")) {
			var messages = getMessagesByCredentials(credential);
			handleMessages(credential, messages);
		}
	}

	private List<EmailMessage> getMessagesByCredentials(final CredentialsEntity credentials) {
		return credentials.getEmailAddress().stream()
			.map(address -> emailService.getAllEmailsInInbox(credentials, address))
			.flatMap(List::stream)
			.toList();
	}

	private void sendSms(final CredentialsEntity credentials, final List<String> validNumbers, final Map<String, String> emailMap) {
		for (var validNumber : validNumbers) {
			var smsRequest = new SmsRequest()
				.sender(Optional.ofNullable(emailMap.get("Sender")).map(sender -> sender.substring(0, min(sender.length(), 11))).orElse("Sundsvall"))
				.message(emailMap.get("Message"))
				.mobileNumber(validNumber);
			messagingIntegration.sendSms(credentials.getMunicipalityId(), smsRequest);
		}
	}

	private void handleMessages(final CredentialsEntity credentials, final List<EmailMessage> messages) throws Exception {
		for (var emailMessage : messages) {
			try {
				var emailMap = ewsIntegration.extractValuesEmailMessage(emailMessage);
				if (emailMap.get("Recipient") == null || emailMap.get("Message") == null) {
					LOG.info("Either 'Recipient' or 'Message' is missing in email. Recipient: {}, Message: {}. Skipping email.", emailMap.get("Recipient"), emailMap.get("Message"));
					ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
					continue;
				}

				var result = ewsIntegration.validateRecipientNumbers(emailMap);
				var validNumbers = result.get("VALID");
				var invalidNumbers = result.get("INVALID");

				if (validNumbers != null) {
					sendSms(credentials, validNumbers, emailMap);
				}
				if (invalidNumbers != null) {
					LOG.info("Can not send sms to invalid numbers: {}", invalidNumbers);
				}
				LOG.debug("Moving sms-email to folder '{}'", credentials.getDestinationFolder());
				ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
			} catch (Exception e) {
				LOG.error("Failed to handle sms-email", e);
				LOG.debug("Moving failed sms-email to folder '{}'", credentials.getDestinationFolder());
				ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
			}
		}
	}
}
