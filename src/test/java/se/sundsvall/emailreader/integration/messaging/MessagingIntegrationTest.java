package se.sundsvall.emailreader.integration.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

	@Mock
	private MessagingClient messagingClient;

	@InjectMocks
	private MessagingIntegration messagingIntegration;

	@Test
	void sendEmail() {

		when(properties.getRecipientAdress()).thenReturn("someAddress");

		messagingIntegration.sendEmail("some message", "someSubject");

		verify(messagingClient, times(1))
			.sendEmail(any(EmailRequest.class));

		verifyNoMoreInteractions(messagingClient);
	}

}
