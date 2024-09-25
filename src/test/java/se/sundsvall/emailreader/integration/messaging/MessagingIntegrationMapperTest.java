package se.sundsvall.emailreader.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

@ExtendWith(MockitoExtension.class)
class MessagingIntegrationMapperTest {

	private final static String EMAIL_MESSAGE_BODY_TEXT = """
		CustomerId = 123456789
		User = testUser
		Password = testPassword
		Message = Nytt lösenord driftkonto: nyttLösenord
		Recipient = 123456789
		""";

	@Mock
	private EmailMessage emailMessageMock;

	@Mock
	private MessageBody messageBodyMock;

	@Test
	void toRequest() {
		var result = MessagingIntegrationMapper.toRequest("someAddress", "some message", "[Warning] EmailReader has detected unhandled emails");

		assertThat(result).isNotNull();
		assertThat(result.getEmailAddress()).isEqualTo("someAddress");
		assertThat(result.getMessage()).isEqualTo("some message");
		assertThat(result.getSubject()).isEqualTo("[Warning] EmailReader has detected unhandled emails");
		assertThat(result.getSender()).isNotNull();
		assertThat(result.getSender().getName()).isEqualTo("EmailReader");
		assertThat(result.getSender().getAddress()).isEqualTo("noreply@emailsender.se");
	}

	@Test
	void extractValuesEmailMessage() throws ServiceLocalException {
		when(emailMessageMock.getBody()).thenReturn(messageBodyMock);
		when(messageBodyMock.toString()).thenReturn(EMAIL_MESSAGE_BODY_TEXT);

		var resultMap = MessagingIntegrationMapper.extractValuesEmailMessage(emailMessageMock);

		assertThat(resultMap).isNotNull();
		assertThat(resultMap.get("CustomerId")).isEqualTo("123456789");
		assertThat(resultMap.get("User")).isEqualTo("testUser");
		assertThat(resultMap.get("Password")).isEqualTo("testPassword");
		assertThat(resultMap.get("Message")).isEqualTo("Nytt lösenord driftkonto: nyttLösenord");
		assertThat(resultMap.get("Recipient")).isEqualTo("123456789");

		verify(emailMessageMock).getBody();
	}

	@Test
	void toSmsRequest() throws ServiceLocalException {
		when(emailMessageMock.getBody()).thenReturn(messageBodyMock);
		when(messageBodyMock.toString()).thenReturn(EMAIL_MESSAGE_BODY_TEXT);

		var smsRequest = MessagingIntegrationMapper.toSmsRequest(emailMessageMock);

		assertThat(smsRequest).isNotNull();
		assertThat(smsRequest.getMessage()).isEqualTo("Nytt lösenord driftkonto: nyttLösenord");
		assertThat(smsRequest.getMobileNumber()).isEqualTo("123456789");
		assertThat(smsRequest.getSender()).isEqualTo("Sundsvalls Kommun");
		assertThat(smsRequest).hasAllNullFieldsOrPropertiesExcept("message", "mobileNumber", "sender");
	}

}
