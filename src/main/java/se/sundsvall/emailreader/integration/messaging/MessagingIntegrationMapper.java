package se.sundsvall.emailreader.integration.messaging;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;

public final class MessagingIntegrationMapper {

	private MessagingIntegrationMapper() {
	}

	public static EmailRequest toRequest(final String recipientAddress, final String message, final String subject) {
		return new EmailRequest(recipientAddress, subject)
			.sender(new EmailSender("EmailReader", "noreply@emailsender.se"))
			.message(message);

	}

}
