package se.sundsvall.emailreader.service.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;

public final class EmailMapper {

	private EmailMapper() {
		// Intentionally empty
	}

	public static List<Email> toEmails(final List<EmailEntity> emailEntities) {
		return emailEntities.stream()
			.map(EmailMapper::toEmail)
			.toList();
	}

	public static Email toEmail(final EmailEntity emailEntity) {

		final var attachments = Optional.ofNullable(emailEntity.getAttachments())
			.orElse(List.of()).stream()
			.map(EmailMapper::toAttachment)
			.toList();

		final var headers = Optional.ofNullable(emailEntity.getHeaders())
			.orElse(List.of()).stream()
			.collect(Collectors.toMap(EmailHeaderEntity::getHeader, EmailHeaderEntity::getValues));

		return Email.builder()
			.withSubject(emailEntity.getSubject())
			.withRecipients(emailEntity.getRecipients())
			.withSender(emailEntity.getSender())
			.withMessage(emailEntity.getMessage())
			.withId(emailEntity.getId())
			.withAttachments(attachments)
			.withReceivedAt(emailEntity.getReceivedAt())
			.withMetadata(emailEntity.getMetadata())
			.withHeaders(headers)
			.build();
	}

	public static EmailEntity toEmailEntity(final Email email, final String namespace, final String municipalityId, final Map<String, String> metadata) {

		final var attachmentEntities = Optional.ofNullable(email.attachments())
			.orElse(List.of()).stream()
			.map(EmailMapper::toAttachmentEntity)
			.toList();

		return EmailEntity.builder()
			.withSubject(email.subject())
			.withRecipients(email.recipients())
			.withSender(email.sender())
			.withMessage(email.message())
			.withAttachments(attachmentEntities)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withReceivedAt(email.receivedAt())
			.withMetadata(metadata)
			.withHeaders(toHeaderEntities(email.headers()))
			.build();
	}

	private static List<EmailHeaderEntity> toHeaderEntities(final Map<Header, List<String>> headers) {

		return Optional.ofNullable(headers)
			.orElseGet(Collections::emptyMap)
			.entrySet()
			.stream()
			.map(entry -> EmailHeaderEntity.builder()
				.withHeader(entry.getKey())
				.withValues(entry.getValue())
				.build())
			.toList();
	}

	private static AttachmentEntity toAttachmentEntity(final Email.Attachment attachment) {

		return AttachmentEntity.builder()
			.withName(attachment.name())
			.withContent(attachment.content())
			.withContentType(attachment.contentType())
			.build();
	}

	public static Email.Attachment toAttachment(final AttachmentEntity attachmentEntity) {

		return Email.Attachment.builder()
			.withName(attachmentEntity.getName())
			.withContent(attachmentEntity.getContent())
			.withContentType(attachmentEntity.getContentType())
			.build();
	}

}
