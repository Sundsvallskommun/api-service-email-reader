package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;
import se.sundsvall.emailreader.utility.BlobBuilder;

@Component
public class GraphMapper {
	private final BlobBuilder blobBuilder;

	public GraphMapper(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	List<EmailEntity> toEmails(final List<Message> messages, final String municipalityId, final String namespace, final Map<String, String> metadata) {
		return messages.stream()
			.map(message -> toEmail(message, municipalityId, namespace, metadata))
			.toList();
	}

	EmailEntity toEmail(final Message message, final String municipalityId, final String namespace, final Map<String, String> metadata) {

		return EmailEntity.builder()
			.withOriginalId(message.getId())
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withRecipients(getRecipients(message))
			.withSender(getSender(message))
			.withSubject(message.getSubject())
			.withHeaders(toHeaders(message))
			.withMessage(getMessage(message))
			.withReceivedAt(message.getReceivedDateTime())
			.withMetadata(metadata != null ? metadata.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)) : null)
			.build();
	}

	List<EmailHeaderEntity> toHeaders(final Message message) {

		final var headers = new ArrayList<EmailHeaderEntity>();

		findHeader(message, Header.MESSAGE_ID.getName())
			.ifPresentOrElse(value -> headers.add(createEmailHeader(Header.MESSAGE_ID, extractValues(value))),
				() -> headers.add(createEmailHeader(Header.MESSAGE_ID, List.of("<" + UUID.randomUUID() + "@randomly-generated>"))));

		findHeader(message, REFERENCES.getName())
			.ifPresent(value -> headers.add(createEmailHeader(REFERENCES, extractValues(value))));

		findHeader(message, IN_REPLY_TO.getName())
			.ifPresent(value -> headers.add(createEmailHeader(IN_REPLY_TO, extractValues(value))));

		findHeader(message, AUTO_SUBMITTED.getName())
			.ifPresent(value -> headers.add(createEmailHeader(AUTO_SUBMITTED, extractValues(value))));

		return headers;
	}

	Optional<String> findHeader(final Message emailMessage, final String headerName) {
		if (emailMessage.getInternetMessageHeaders() == null) {
			return Optional.empty();
		}

		return emailMessage.getInternetMessageHeaders().stream()
			.filter(header -> headerName.equalsIgnoreCase(header.getName()))
			.map(InternetMessageHeader::getValue)
			.filter(Objects::nonNull)
			.findFirst();
	}

	List<String> extractValues(final String input) {
		return Optional.ofNullable(input)
			.map(inputString -> Pattern.compile(" ")
				.splitAsStream(inputString)
				.toList())
			.orElse(emptyList());
	}

	EmailHeaderEntity createEmailHeader(final Header header, final List<String> values) {
		return EmailHeaderEntity.builder().withHeader(header).withValues(values).build();
	}

	String getMessage(final Message message) {
		return Optional.ofNullable(message.getBody())
			.map(ItemBody::getContent)
			.orElse(null);
	}

	String getSender(final Message message) {
		return Optional.ofNullable(message.getSender())
			.flatMap(senderObj -> Optional.ofNullable(senderObj.getEmailAddress())
				.map(EmailAddress::getAddress))
			.orElse(null);
	}

	List<String> getRecipients(final Message message) {
		return Optional.ofNullable(message.getToRecipients())
			.orElse(emptyList())
			.stream()
			.filter(Objects::nonNull)
			.map(recipient -> Optional.ofNullable(recipient.getEmailAddress())
				.map(EmailAddress::getAddress)
				.orElse(null))
			.filter(Objects::nonNull)
			.toList();
	}

	public List<AttachmentEntity> toAttachments(final List<Attachment> attachments) {
		return Optional.ofNullable(attachments)
			.orElse(emptyList()).stream()
			.map(attachment -> {
				if (attachment instanceof final FileAttachment fileAttachment) {
					return toAttachment(fileAttachment);
				}
				return null;
			}).toList();
	}

	private AttachmentEntity toAttachment(final FileAttachment attachment) {

		return AttachmentEntity.builder()
			.withName(attachment.getName())
			.withContent(blobBuilder.createBlob(attachment.getContentBytes()))
			.withContentType(attachment.getContentType())
			.build();
	}

}
