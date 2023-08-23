package se.sundsvall.emailreader.integration;


import java.util.List;

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

@Component
public class EWSMapper {

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

        return Email.builder()
            .withSubject(emailMessage.getSubject())
            .withFrom(emailMessage.getFrom().getAddress())
            .withTo(recipients)
            .withMessage(emailMessage.getBody().toString())
            .withId(String.valueOf(emailMessage.getId()))
            .build();

    }

}
