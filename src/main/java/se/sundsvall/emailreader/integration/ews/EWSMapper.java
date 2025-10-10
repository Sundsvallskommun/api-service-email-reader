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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeader;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeaderCollection;
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

	List<EmailHeaderEntity> toHeaders(final EmailMessage emailMessage) {

		try {
			final var headers = new ArrayList<EmailHeaderEntity>();

			final var internetMessageHeaders = emailMessage.getInternetMessageHeaders();
			findHeader(internetMessageHeaders, MESSAGE_ID).ifPresentOrElse(header -> headers.add(createEmailHeader(MESSAGE_ID, extractValues(header.getValue().strip()))),
				() -> headers.add(createEmailHeader(MESSAGE_ID, List.of("<" + UUID.randomUUID() + "@randomly-generated>"))));
			findHeader(internetMessageHeaders, REFERENCES).ifPresent(header -> headers.add(createEmailHeader(REFERENCES, extractValues(header.getValue().strip()))));
			findHeader(internetMessageHeaders, IN_REPLY_TO).ifPresent(header -> headers.add(createEmailHeader(IN_REPLY_TO, extractValues(header.getValue().strip()))));
			findHeader(internetMessageHeaders, AUTO_SUBMITTED).ifPresent(header -> headers.add(createEmailHeader(AUTO_SUBMITTED, extractValues(header.getValue().strip()))));

			return headers;
		} catch (final Exception e) {
			LOG.warn("Could not load headers", e);
			return emptyList();
		}
	}

	Optional<InternetMessageHeader> findHeader(final InternetMessageHeaderCollection collection, final Header header) {
		for (final var internetMessageHeader : collection) {
			if (internetMessageHeader.getName().equalsIgnoreCase(header.getName())) {
				return Optional.of(internetMessageHeader);
			}
		}
		return Optional.empty();
	}

	EmailHeaderEntity createEmailHeader(final Header header, final List<String> values) {
		return EmailHeaderEntity.builder()
			.withHeader(header)
			.withValues(values)
			.build();
	}

	List<String> extractValues(final String input) {
		return Optional.ofNullable(input)
			.map(inputString -> Pattern.compile(" ")
				.splitAsStream(inputString)
				.toList())
			.orElse(emptyList());
	}

	public EmailEntity toEmail(final EmailMessage emailMessage, final String municipalityId, final String namespace, final Map<String, String> metadata) throws ServiceLocalException {

		if (emailMessage == null) {
			return null;
		}

		final var recipients = emailMessage.getToRecipients().getItems().stream()
			.map(EmailAddress::getAddress)
			.toList();

		final var attachments = Optional.ofNullable(emailMessage.getAttachments()).stream()
			.flatMap(emailAttachments -> emailAttachments.getItems().stream())
			.filter(FileAttachment.class::isInstance)
			.map(FileAttachment.class::cast)
			.map(this::toAttachment)
			.toList();

		final var receivedAt = Optional.ofNullable(emailMessage.getDateTimeReceived())
			.map(Date::toInstant)
			.map(instant -> instant.atZone(ZoneId.systemDefault().getRules().getOffset(instant)))
			.map(OffsetDateTime::from)
			.orElse(null);

		final var emailEntity = EmailEntity.builder()
			.withOriginalId(emailMessage.getId().getUniqueId())
			.withSubject(emailMessage.getSubject())
			.withSender(emailMessage.getFrom().getAddress())
			.withRecipients(recipients)
			.withAttachments(attachments)
			.withReceivedAt(receivedAt)
			.withHeaders(toHeaders(emailMessage))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata != null ? metadata.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null)
			.build();

		// Normalize Windows CRLF and collapse excessive blank lines to avoid double linebreaks from Windows senders
		final var normalizedBody = Optional.ofNullable(emailMessage.getBody())
			.map(body -> body.toString().replace("\r\n\r\n", "\n"))
			.orElse(null);

		emailEntity.setMessage(normalizedBody);

		return emailEntity;
	}

	AttachmentEntity toAttachment(final FileAttachment fileAttachment) {

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
