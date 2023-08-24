package se.sundsvall.emailreader.integration;


import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.sundsvall.emailreader.api.model.Email;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

@Component
public class EWSMapper {

    private final Logger log = LoggerFactory.getLogger(EWSMapper.class);

    private final PropertySet propertySetTextBody = new PropertySet(BasePropertySet.FirstClassProperties,
        ItemSchema.Body, ItemSchema.Subject, ItemSchema.Attachments,
        EmailMessageSchema.From, EmailMessageSchema.ToRecipients, ItemSchema.DisplayTo);

    public EWSMapper() {
        propertySetTextBody.setRequestedBodyType(BodyType.Text);
    }

    Email toEmail(final ExchangeService service, final EmailMessage emailMessage) throws Exception {

        service.loadPropertiesForItems(List.of(emailMessage), propertySetTextBody);

        final var recipients = emailMessage.getToRecipients().getItems().stream()
            .map(EmailAddress::getAddress)
            .toList();

        final var attachments = emailMessage.getAttachments().getItems().stream()
            .filter(FileAttachment.class::isInstance)
            .map(FileAttachment.class::cast)
            .map(this::toAttachment)
            .toList();

        return Email.builder()
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
