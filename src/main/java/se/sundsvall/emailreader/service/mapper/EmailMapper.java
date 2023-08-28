package se.sundsvall.emailreader.service.mapper;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;


@Component
public class EmailMapper {

    public List<Email> toEmails(final List<EmailEntity> emailEntities) {

        return emailEntities.stream()
            .map(this::toEmail)
            .toList();
    }

    public Email toEmail(final EmailEntity emailEntity) {

        final var attachments = Optional.ofNullable(emailEntity.getAttachments())
            .orElse(List.of()).stream()
            .map(this::toAttachment)
            .toList();

        return Email.builder()
            .withSubject(emailEntity.getSubject())
            .withTo(emailEntity.getTo())
            .withFrom(emailEntity.getFrom())
            .withMessage(emailEntity.getMessage())
            .withId(emailEntity.getId())
            .withAttachments(attachments)
            .build();
    }

    public Email.Attachment toAttachment(final AttachmentEntity attachmentEntity) {

        return Email.Attachment.builder()
            .withName(attachmentEntity.getName())
            .withContent(attachmentEntity.getContent())
            .withContentType(attachmentEntity.getContentType())
            .build();
    }

}
