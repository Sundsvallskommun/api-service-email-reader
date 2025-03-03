package se.sundsvall.emailreader.integration.ews;

import static java.util.Collections.emptyList;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;
import static se.sundsvall.emailreader.utility.ServiceUtil.detectMimeType;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;
import se.sundsvall.emailreader.utility.BlobBuilder;

@Component
public final class EWSMapper {

	private static final Logger LOG = LoggerFactory.getLogger(EWSMapper.class);
	private final BlobBuilder blobBuilder;

	public EWSMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	private static List<EmailHeaderEntity> toHeaders(final EmailMessage emailMessage) {

		try {
			final var headers = new ArrayList<EmailHeaderEntity>();

			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(MESSAGE_ID.getName())).ifPresentOrElse(value -> headers.add(createEmailHeader(MESSAGE_ID, extractValues(value.getValue()))),
				() -> headers.add(createEmailHeader(MESSAGE_ID, List.of("<" + UUID.randomUUID() + "@randomly-generated>"))));
			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(REFERENCES.getName())).ifPresent(value -> headers.add(createEmailHeader(REFERENCES, extractValues(value.getValue()))));
			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(IN_REPLY_TO.getName())).ifPresent(value -> headers.add(createEmailHeader(IN_REPLY_TO, extractValues(value.getValue()))));
			Optional.ofNullable(emailMessage.getInternetMessageHeaders().find(AUTO_SUBMITTED.getName())).ifPresent(value -> headers.add(createEmailHeader(AUTO_SUBMITTED, extractValues(value.getValue()))));

			return headers;
		} catch (final Exception e) {
			LOG.warn("Could not load headers", e);
			return emptyList();
		}
	}

	private static EmailHeaderEntity createEmailHeader(final Header header, final List<String> values) {
		return EmailHeaderEntity.builder().withHeader(header).withValues(values).build();
	}

	private static List<String> extractValues(final String input) {
		return Optional.ofNullable(input)
			.map(inputString -> Pattern.compile(" ")
				.splitAsStream(inputString)
				.toList())
			.orElse(emptyList());
	}

	public List<EmailEntity> toEmails(final List<EmailMessage> emailMessages, final String municipalityId, final String namespace, final Map<String, String> metadata) {
		return emailMessages.stream()
			.map(emailMessage -> toEmail(emailMessage, municipalityId, namespace, metadata))
			.filter(Objects::nonNull)
			.toList();
	}

	private EmailEntity toEmail(final EmailMessage emailMessage, final String municipalityId, final String namespace, final Map<String, String> metadata) {
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

			return EmailEntity.builder()
				.withOriginalId(emailMessage.getId().getUniqueId())
				.withSubject(emailMessage.getSubject())
				.withSender(emailMessage.getFrom().getAddress())
				.withRecipients(recipients)
				.withMessage(emailMessage.getBody().toString())
				.withAttachments(attachments)
				.withReceivedAt(receivedAt)
				.withHeaders(toHeaders(emailMessage))
				.withMunicipalityId(municipalityId)
				.withNamespace(namespace)
				.withMetadata(metadata != null ? metadata.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null)
				.build();
		} catch (final ServiceLocalException e) {
			LOG.warn("Could not load email", e);
			return null;
		}
	}

	private AttachmentEntity toAttachment(final FileAttachment fileAttachment) {

		try {
			fileAttachment.load();
			return AttachmentEntity.builder()
				.withName(fileAttachment.getName())
				.withContent(blobBuilder.createBlob(fileAttachment.getContent()))
				.withContentType(detectMimeType(fileAttachment.getName(), fileAttachment.getContent()))
				.build();
		} catch (final Exception e) {
			LOG.warn("Could not load attachment", e);
			return null;
		}
	}
}
