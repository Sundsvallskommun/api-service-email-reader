package se.sundsvall.emailreader.service.scheduler;

import generated.se.sundsvall.messaging.SmsRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.EmailService;

import static java.lang.Math.min;

@Component
public class EwsScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EwsScheduler.class);
	private final EmailService emailService;
	private final MessagingIntegration messagingIntegration;
	private final EWSIntegration ewsIntegration;
	private final Consumer<String> emailSetUnHealthyConsumer;
	private final Consumer<String> smsSetUnHealthyConsumer;

	@Value("${scheduled.check-for-new-emails.ews.name}")
	private String emailJobName;
	@Value("${scheduled.check-for-new-sms-emails.name}")
	private String smsJobName;

	public EwsScheduler(final EmailService emailService, final MessagingIntegration messagingIntegration, final EWSIntegration ewsIntegration, final Dept44HealthUtility dept44HealthUtility) {
		this.emailService = emailService;
		this.messagingIntegration = messagingIntegration;
		this.ewsIntegration = ewsIntegration;
		this.emailSetUnHealthyConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(emailJobName, String.format("Email error: %s", msg));
		this.smsSetUnHealthyConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(smsJobName, String.format("Email error: %s", msg));
	}

	@Dept44Scheduled(
		cron = "${scheduled.check-for-new-emails.ews.cron}",
		name = "${scheduled.check-for-new-emails.ews.name}",
		lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduled.maximum-execution-time}")
	public void checkForNewEmails() {
		for (final var credential : emailService.findAllByActionAndActive("PERSIST")) {
			for (final var address : credential.getEmailAddress()) {
				LOG.info("[{}]: Fetching mails for address", address);
				final var emails = emailService.getAllEmailsInInbox(credential, address, emailSetUnHealthyConsumer);
				LOG.info("[{}]: Fetched {} emails", address, emails.size());
				for (final var email : emails) {
					try {
						LOG.info("[{}]: Processing email with id '{}'", address, email.getId());
						emailService.saveAndMoveEmail(email, address, credential, emailSetUnHealthyConsumer);
					} catch (final Exception e) {
						LOG.error("[{}]: Failed to handle individual email", address, e);
						emailSetUnHealthyConsumer.accept("Failed to handle individual email for " + address);
					}
				}
				LOG.info("Done fetching mails for address '{}'.", address);
			}
		}
	}

	@Dept44Scheduled(cron = "${scheduled.check-for-old-emails.cron}",
		name = "${scheduled.check-for-old-emails.name}",
		lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduled.maximum-execution-time}")
	void checkForOldEmailsAndSendReport() {
		emailService.sendReport();
	}

	@Dept44Scheduled(cron = "${scheduled.check-for-new-sms-emails.cron}",
		name = "${scheduled.check-for-new-sms-emails.name}",
		lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduled.maximum-execution-time}")
	void checkForNewSmsEmails() throws Exception {
		for (final var credential : emailService.findAllByActionAndActive("SEND_SMS")) {
			final var messages = getMessagesByCredentials(credential);
			LOG.info("Fetched {} sms-emails for address '{}'", messages.size(), credential.getEmailAddress());
			handleMessages(credential, messages);
		}
	}

	private List<EmailMessage> getMessagesByCredentials(final CredentialsEntity credentials) {
		return credentials.getEmailAddress().stream()
			.map(address -> emailService.getAllEmailsInInbox(credentials, address, smsSetUnHealthyConsumer))
			.flatMap(List::stream)
			.toList();
	}

	private void sendSms(final CredentialsEntity credentials, final List<String> validNumbers, final Map<String, String> emailMap) {
		for (final var validNumber : validNumbers) {
			final var smsRequest = new SmsRequest()
				.sender(Optional.ofNullable(emailMap.get("Sender")).map(sender -> sender.substring(0, min(sender.length(), 11))).orElse("Sundsvall"))
				.message(emailMap.get("Message"))
				.mobileNumber(validNumber)
				.priority(SmsRequest.PriorityEnum.HIGH);
			messagingIntegration.sendSms(credentials.getMunicipalityId(), smsRequest);
		}
	}

	private void handleMessages(final CredentialsEntity credentials, final List<EmailMessage> messages) throws Exception {
		for (final var emailMessage : messages) {
			try {
				final var emailMap = ewsIntegration.extractValuesEmailMessage(emailMessage);
				final var message = emailMap.get("Message");
				if (emailMap.get("Recipient") == null || message == null) {
					final var recipient = emailMap.get("Recipient");
					smsSetUnHealthyConsumer.accept("Recipient or Message is missing in email");
					LOG.info("Either 'Recipient' or 'Message' is missing in email. Recipient: {}, Message: {}. Skipping email.", recipient, message);
					ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
					continue;
				}

				final var result = ewsIntegration.validateRecipientNumbers(emailMap);
				final var validNumbers = result.get("VALID");
				final var invalidNumbers = result.get("INVALID");

				if (validNumbers != null) {
					sendSms(credentials, validNumbers, emailMap);
				}
				if (invalidNumbers != null) {
					smsSetUnHealthyConsumer.accept("Can not send sms to invalid numbers");
					LOG.info("Can not send sms to invalid numbers: {}", invalidNumbers);
				}
				LOG.debug("Moving sms-email to folder '{}'", credentials.getDestinationFolder());
				ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
			} catch (final Exception e) {
				smsSetUnHealthyConsumer.accept("Failed to handle sms-email");
				LOG.error("Failed to handle sms-email", e);
				LOG.debug("Moving failed sms-email to folder '{}'", credentials.getDestinationFolder());
				ewsIntegration.moveEmail(emailMessage.getId(), emailMessage.getReceivedBy().getAddress(), credentials.getDestinationFolder());
			}
		}
	}
}
