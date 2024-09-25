package se.sundsvall.emailreader.integration.messaging;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.zalando.problem.Problem;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.EmailSender;
import generated.se.sundsvall.messaging.SmsRequest;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

public final class MessagingIntegrationMapper {

	private MessagingIntegrationMapper() {}

	public static EmailRequest toRequest(final String recipientAddress, final String message, final String subject) {
		return new EmailRequest(recipientAddress, subject)
			.sender(new EmailSender("EmailReader", "noreply@emailsender.se"))
			.message(message);
	}

	public static SmsRequest toSmsRequest(final EmailMessage emailMessage) {

		var values = extractValuesEmailMessage(emailMessage);

		return new SmsRequest()
			.sender("Sundsvalls Kommun")
			.message(values.get("Message"))
			.mobileNumber(values.get("Recipient"));
	}

	static Map<String, String> extractValuesEmailMessage(final EmailMessage emailMessage) {
		try {
			return Arrays.stream(emailMessage.getBody().toString().split("\n"))
				.map(line -> line.split("=", 2))
				.filter(pairs -> pairs.length == 2)
				.collect(Collectors.toMap(
					pairs -> pairs[0].trim(),
					pairs -> pairs[1].trim()
				));
		} catch (ServiceLocalException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to parse email body: " + e.getMessage());
		}
	}

}
