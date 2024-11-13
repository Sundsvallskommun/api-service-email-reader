package se.sundsvall.emailreader.integration.ews;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmail;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;
import static se.sundsvall.emailreader.integration.ews.EWSMapper.toEmail;
import static se.sundsvall.emailreader.integration.ews.EWSMapper.toEmails;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeader;
import microsoft.exchange.webservices.data.property.complex.InternetMessageHeaderCollection;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

@ExtendWith({
	MockitoExtension.class
})
class EWSMapperTest {

	@Test
	void testToEmail() throws Exception {

		// Mock
		final var emailMessage = mock(EmailMessage.class);
		final var internetMessageHeaderCollection = mock(InternetMessageHeaderCollection.class);
		final var internetMessageHeader = mock(InternetMessageHeader.class);

		when(emailMessage.getToRecipients()).thenReturn(new EmailAddressCollection());
		emailMessage.getToRecipients().add("recipient@example.com");

		when(emailMessage.getInternetMessageHeaders()).thenReturn(internetMessageHeaderCollection);
		when(emailMessage.getInternetMessageHeaders().find(anyString())).thenReturn(internetMessageHeader);
		when(internetMessageHeaderCollection.find(anyString()).getValue()).thenReturn("<Test1@Test1.se> <Test2@Test2.se> <Test3@Test3.se>");

		when(emailMessage.getId()).thenReturn(new ItemId("123456789"));
		when(emailMessage.getBody()).thenReturn(new MessageBody("Mocked email body"));
		when(emailMessage.getSubject()).thenReturn("Test Email Subject");
		when(emailMessage.getFrom()).thenReturn(new EmailAddress("test", "sender@example.com"));
		when(emailMessage.getDateTimeReceived()).thenReturn(Date.from(Instant.now()));

		when(emailMessage.getAttachments()).thenReturn(new AttachmentCollection());
		final var attachments = emailMessage.getAttachments();
		final var fileAttachment = mock(FileAttachment.class);
		when(fileAttachment.getName()).thenReturn("Mocked attachment");
		when(fileAttachment.getContent()).thenReturn("mockedfile.jpg".getBytes());
		attachments.getItems().add(fileAttachment);

		// Act
		final var result = toEmail(emailMessage);

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata");
		assertThat(result.sender()).isEqualTo("sender@example.com");
		assertThat(result.recipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.subject()).isEqualTo("Test Email Subject");
		assertThat(result.message()).isEqualTo("Mocked email body");
		assertThat(result.receivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.id()).isNotNull().isNotEmpty();
		assertThat(result.attachments()).hasSize(1).satisfies(
			attachment -> {
				assertThat(attachment.getFirst().name()).isEqualTo("Mocked attachment");
				assertThat(attachment.getFirst().contentType()).isEqualTo("text/plain");
				assertThat(attachment.getFirst().content()).isEqualTo(Base64.getEncoder().encodeToString("mockedfile.jpg".getBytes()));
			});
		assertThat(result.headers()).hasSize(4).satisfies(
			headers -> {
				assertThat(headers.get(MESSAGE_ID)).hasSize(3).satisfies(
					messageId -> assertThat(messageId).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(IN_REPLY_TO)).hasSize(3).satisfies(
					inReplyTo -> assertThat(inReplyTo).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(REFERENCES)).hasSize(3).satisfies(
					references -> assertThat(references).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(AUTO_SUBMITTED)).hasSize(3).satisfies(
					references -> assertThat(references).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
			});
	}

	@Test
	void testToEmail_receivedAtNull() throws Exception {

		// Mock
		final var emailMessage = mock(EmailMessage.class);

		when(emailMessage.getToRecipients()).thenReturn(new EmailAddressCollection());
		emailMessage.getToRecipients().add("recipient@example.com");

		final var internetMessageHeaderCollection = mock(InternetMessageHeaderCollection.class);
		final var internetMessageHeader = mock(InternetMessageHeader.class);

		when(emailMessage.getInternetMessageHeaders()).thenReturn(internetMessageHeaderCollection);
		when(emailMessage.getInternetMessageHeaders().find(anyString())).thenReturn(internetMessageHeader);
		when(internetMessageHeaderCollection.find(anyString()).getValue()).thenReturn("<Test1@Test1.se> <Test2@Test2.se> <Test3@Test3.se>");

		when(emailMessage.getId()).thenReturn(new ItemId("123456789"));
		when(emailMessage.getBody()).thenReturn(new MessageBody("Mocked email body"));
		when(emailMessage.getSubject()).thenReturn("Test Email Subject");
		when(emailMessage.getFrom()).thenReturn(new EmailAddress("test", "sender@example.com"));
		when(emailMessage.getDateTimeReceived()).thenReturn(null);

		when(emailMessage.getAttachments()).thenReturn(new AttachmentCollection());
		final var attachments = emailMessage.getAttachments();
		final var fileAttachment = mock(FileAttachment.class);
		when(fileAttachment.getName()).thenReturn("Mocked attachment");
		when(fileAttachment.getContent()).thenReturn("mockedfile.jpg".getBytes());
		attachments.getItems().add(fileAttachment);

		// Act
		final var result = toEmail(emailMessage);

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata", "receivedAt");

		assertThat(result.sender()).isEqualTo("sender@example.com");
		assertThat(result.recipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.subject()).isEqualTo("Test Email Subject");
		assertThat(result.message()).isEqualTo("Mocked email body");
		assertThat(result.receivedAt()).isNull();
		assertThat(result.id()).isNotNull().isNotEmpty();
		assertThat(result.attachments()).hasSize(1).satisfies(
			attachment -> {
				assertThat(attachment.getFirst().name()).isEqualTo("Mocked attachment");
				assertThat(attachment.getFirst().contentType()).isEqualTo("text/plain");
				assertThat(attachment.getFirst().content()).isEqualTo(Base64.getEncoder().encodeToString("mockedfile.jpg".getBytes()));
			});
		assertThat(result.headers()).hasSize(4).satisfies(
			headers -> {
				assertThat(headers.get(MESSAGE_ID)).hasSize(3).satisfies(
					messageId -> assertThat(messageId).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(IN_REPLY_TO)).hasSize(3).satisfies(
					inReplyTo -> assertThat(inReplyTo).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(REFERENCES)).hasSize(3).satisfies(
					references -> assertThat(references).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
				assertThat(headers.get(AUTO_SUBMITTED)).hasSize(3).satisfies(
					references -> assertThat(references).isEqualTo(List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
			});
	}

	@Test
	void testToEmail_noHeaders_noAttachments() throws Exception {

		// Mock
		final var emailMessage = mock(EmailMessage.class);

		when(emailMessage.getToRecipients()).thenReturn(new EmailAddressCollection());
		emailMessage.getToRecipients().add("recipient@example.com");

		when(emailMessage.getId()).thenReturn(new ItemId("123456789"));
		when(emailMessage.getBody()).thenReturn(new MessageBody("Mocked email body"));
		when(emailMessage.getSubject()).thenReturn("Test Email Subject");
		when(emailMessage.getFrom()).thenReturn(new EmailAddress("test", "sender@example.com"));
		when(emailMessage.getDateTimeReceived()).thenReturn(Date.from(Instant.now()));
		when(emailMessage.getInternetMessageHeaders()).thenReturn(new InternetMessageHeaderCollection());

		// Act
		final var result = toEmail(emailMessage);

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata", "headers", "attachments");

		assertThat(result.sender()).isEqualTo("sender@example.com");
		assertThat(result.recipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.subject()).isEqualTo("Test Email Subject");
		assertThat(result.message()).isEqualTo("Mocked email body");
		assertThat(result.receivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.id()).isNotNull().isNotEmpty();
		assertThat(result.attachments()).isEmpty();
		assertThat(result.headers()).hasSize(1);
	}

	@Test
	void testToEmail_throwException() throws Exception {

		// Mock
		final var emailMessage = mock(EmailMessage.class);
		when(emailMessage.getToRecipients()).thenThrow(new ServiceLocalException("Something went wrong"));
		// Act
		final var result = toEmail(emailMessage);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toEmailsTest() {
		var staticMock = mockStatic(EWSMapper.class);
		var emailMessage = mock(EmailMessage.class);
		var emailMessages = List.of(emailMessage);
		var email = createEmail(null);

		staticMock.when(() -> toEmail(emailMessage)).thenReturn(email);
		staticMock.when(() -> toEmails(any())).thenCallRealMethod();

		var result = toEmails(emailMessages);

		assertThat(result).hasSize(1).containsExactly(email);
		staticMock.close();
	}
}
