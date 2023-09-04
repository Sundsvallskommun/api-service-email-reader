package se.sundsvall.emailreader.integration.messaging;

import org.springframework.stereotype.Component;

@Component
public class MessagingIntegration {

	private final MessagingClient messagingClient;

	private final MessagingProperties properties;

	public MessagingIntegration(final MessagingClient messagingClient, final MessagingProperties properties) {
		this.messagingClient = messagingClient;
		this.properties = properties;
	}

	public void sendEmail(final String message, final String emailSubject) {

		messagingClient.sendEmail(MessagingIntegrationMapper.toRequest(properties.getRecipientAdress(), message, emailSubject));
	}

}
