package se.sundsvall.emailreader.integration.ews;


import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.api.model.Header;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

@Component
public class EWSMapper {

	private final Logger log = LoggerFactory.getLogger(EWSMapper.class);

	Email toEmail(final EmailMessage emailMessage) {
		try {

			final var recipients = emailMessage.getToRecipients().getItems().stream()
				.map(EmailAddress::getAddress)
				.toList();

			final var attachments = Optional.ofNullable(emailMessage.getAttachments())
				.map(emailAttachments -> emailAttachments.getItems().stream())
				.orElseGet(Stream::empty)
				.filter(FileAttachment.class::isInstance)
				.map(FileAttachment.class::cast)
				.map(this::toAttachment)
				.toList();

			final var receivedAt = Optional.ofNullable(emailMessage.getDateTimeReceived())
				.map(Date::toInstant)
				.map(instant -> instant.atZone(ZoneId.systemDefault().getRules().getOffset(instant)))
				.map(OffsetDateTime::from)
				.orElse(null);

			return Email.builder()
				.withId(emailMessage.getId().getUniqueId())
				.withSubject(emailMessage.getSubject())
				.withSender(emailMessage.getFrom().getAddress())
				.withRecipients(recipients)
				.withMessage(emailMessage.getBody().toString())
				.withId(String.valueOf(emailMessage.getId()))
				.withAttachments(attachments)
				.withReceivedAt(receivedAt)
				.withHeaders(toHeaders(emailMessage))
				.build();
		} catch (final ServiceLocalException e) {
			log.warn("Could not load email", e);
			return null;
		}
	}

	private Email.Attachment toAttachment(final FileAttachment fileAttachment) {

		try {
			fileAttachment.load();
			return Email.Attachment.builder()
				.withName(fileAttachment.getName())
				.withContent(Base64.getEncoder().encodeToString(fileAttachment.getContent()))
				.withContentType(fileAttachment.getContentType())
				.build();
		} catch (final Exception e) {
			log.warn("Could not load attachment", e);
			return null;

		}
	}

	private Map<Header, List<String>> toHeaders(final EmailMessage emailMessage) {

		try {
			final var headers = new EnumMap<Header, List<String>>(Header.class);

			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(MESSAGE_ID.getName())).ifPresent(value -> headers.put(MESSAGE_ID, extractValues(value.getValue())));
			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(REFERENCES.getName())).ifPresent(value -> headers.put(REFERENCES, extractValues(value.getValue())));
			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(IN_REPLY_TO.getName())).ifPresent(value -> headers.put(IN_REPLY_TO, extractValues(value.getValue())));

			return headers;
		} catch (final Exception e) {
			log.warn("Could not load headers", e);
			return Collections.emptyMap();
		}
	}

	private List<String> extractValues(final String input) {
		return Optional.ofNullable(input)
			.map(inputString -> Pattern.compile(" ")
				.splitAsStream(inputString)
				.toList())
			.orElse(Collections.emptyList());
	}

}
