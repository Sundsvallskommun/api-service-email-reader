package se.sundsvall.emailreader.service.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.sundsvall.emailreader.api.model.Email;
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
			.withHtmlMessage(emailEntity.getHtmlMessage())
			.withId(emailEntity.getId())
			.withAttachments(attachments)
			.withReceivedAt(emailEntity.getReceivedAt())
			.withMetadata(emailEntity.getMetadata())
			.withHeaders(headers)
			.build();
	}

	public static Email.Attachment toAttachment(final AttachmentEntity attachmentEntity) {

		return Email.Attachment.builder()
			.withId(attachmentEntity.getId())
			.withName(attachmentEntity.getName())
			.withContentType(attachmentEntity.getContentType())
			.build();
	}

}
