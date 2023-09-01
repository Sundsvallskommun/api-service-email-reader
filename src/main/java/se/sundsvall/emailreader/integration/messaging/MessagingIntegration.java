package se.sundsvall.emailreader.integration.messaging;

import org.springframework.stereotype.Component;

@Component
public class MessagingIntegration {

    static final String INTEGRATION_NAME = "Messaging";

    private final MessagingClient messagingClient;

    private final MessagingProperties properties;

    private final MessagingIntegrationMapper mapper = new MessagingIntegrationMapper();

    public MessagingIntegration(final MessagingClient messagingClient, final MessagingProperties properties) {
        this.messagingClient = messagingClient;
        this.properties = properties;
    }

    public void sendEmail(final String message, final String emailSubject) {

        messagingClient.sendEmail(mapper.toRequest(properties.getRecipientAdress(), message, emailSubject));
    }

}
