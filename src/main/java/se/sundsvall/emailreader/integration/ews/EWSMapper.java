package se.sundsvall.emailreader.integration.ews;


import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.emailreader.api.model.Email;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

public class EWSMapper {

    private final Logger log = LoggerFactory.getLogger(EWSMapper.class);

    Email toEmail(final EmailMessage emailMessage) throws ServiceLocalException {

        final var recipients = emailMessage.getToRecipients().getItems().stream()
            .map(EmailAddress::getAddress)
            .toList();

        final var attachments = emailMessage.getAttachments().getItems().stream()
            .filter(FileAttachment.class::isInstance)
            .map(FileAttachment.class::cast)
            .map(this::toAttachment)
            .toList();

        return Email.builder()
            .withId(emailMessage.getId().getUniqueId())
            .withSubject(emailMessage.getSubject())
            .withFrom(emailMessage.getFrom().getAddress())
            .withTo(recipients)
            .withMessage(emailMessage.getBody().toString())
            .withId(String.valueOf(emailMessage.getId()))
            .withAttachments(attachments)
            .build();
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

}
