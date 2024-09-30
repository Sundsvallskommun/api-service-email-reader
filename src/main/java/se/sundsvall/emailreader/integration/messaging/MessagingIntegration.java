package se.sundsvall.emailreader.integration.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.messaging.SmsRequest;

@Component
public class MessagingIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(MessagingIntegration.class);
	private static final String ORIGIN = "EMAIL_READER";
	private static final boolean ASYNC = true;

	private final MessagingClient messagingClient;

	private final MessagingProperties properties;

	public MessagingIntegration(final MessagingClient messagingClient, final MessagingProperties properties) {
		this.messagingClient = messagingClient;
		this.properties = properties;
	}

	public void sendEmail(final String municipalityId, final String message, final String emailSubject) {
		messagingClient.sendEmail(municipalityId, MessagingIntegrationMapper.toRequest(properties.getRecipientAdress(), message, emailSubject));
	}

	public void sendSms(final String municipalityId, final SmsRequest smsRequest) {
		LOG.info("Sending sms to {}", smsRequest.getMobileNumber());
		messagingClient.sendSms(ORIGIN, municipalityId, smsRequest, ASYNC);
	}

}
