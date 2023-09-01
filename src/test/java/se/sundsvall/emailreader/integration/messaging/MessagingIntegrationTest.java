package se.sundsvall.emailreader.integration.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.messaging.EmailRequest;


@ExtendWith(MockitoExtension.class)
class MessagingIntegrationTest {


    @Mock
    private MessagingProperties properties;


    private MessagingIntegrationMapper mapper;

    @Mock
    private MessagingClient messagingClient;

    @InjectMocks
    private MessagingIntegration messagingIntegration;


    @BeforeEach
    public void setup() throws Exception {

        mapper = mock(MessagingIntegrationMapper.class);
        final var mapperField = messagingIntegration.getClass().getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(messagingIntegration, mapper);
    }

    @Test
    void sendEmail() {

        when(properties.getRecipientAdress()).thenReturn("someAddress");
        when(mapper.toRequest(any(String.class), any(String.class), any(String.class)))
            .thenReturn(new EmailRequest("someAddress", "some message"));

        messagingIntegration.sendEmail("some message", "someSubject");

        verify(messagingClient, times(1))
            .sendEmail(any(EmailRequest.class));
        verify(mapper, times(1))
            .toRequest(any(String.class), any(String.class), any(String.class));
        verifyNoMoreInteractions(messagingClient, mapper);
    }

}
