package se.sundsvall.emailreader.integration.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.SmsRequest;

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

		messagingIntegration.sendEmail("2281", "some message", "someSubject");

		verify(messagingClient).sendEmail(eq("2281"), any(EmailRequest.class));
		verifyNoMoreInteractions(messagingClient);
	}

	@Test
	void sendSms() {
		var municipalityId = "2281";
		var smsRequest = new SmsRequest().mobileNumber("07012345678");

		messagingIntegration.sendSms(municipalityId, smsRequest);

		verify(messagingClient).sendSms("EMAIL_READER", municipalityId, smsRequest, true);
		verifyNoMoreInteractions(messagingClient);
	}

}
