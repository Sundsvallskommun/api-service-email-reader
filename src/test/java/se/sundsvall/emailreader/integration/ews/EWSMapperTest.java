package se.sundsvall.emailreader.integration.ews;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;
import static se.sundsvall.emailreader.integration.ews.EWSMapper.toEmails;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({
	MockitoExtension.class
})
class EWSMapperTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String NAMESPACE = "namespace";
	private static final Map<String, String> METADATA = Map.of("key", "value");

	@Test
	void testToEmails() throws Exception {

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
		final var result = toEmails(List.of(emailMessage), MUNICIPALITY_ID, NAMESPACE, METADATA).getFirst();

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("createdAt");
		assertThat(result.getSender()).isEqualTo("sender@example.com");
		assertThat(result.getRecipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.getSubject()).isEqualTo("Test Email Subject");
		assertThat(result.getMessage()).isEqualTo("Mocked email body");
		assertThat(result.getReceivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.getId()).isNotNull().isNotEmpty();
		assertThat(result.getAttachments()).hasSize(1).satisfies(
			attachment -> {
				assertThat(attachment.getFirst().getName()).isEqualTo("Mocked attachment");
				assertThat(attachment.getFirst().getContentType()).isEqualTo("text/plain");
				assertThat(attachment.getFirst().getContent()).isEqualTo(Base64.getEncoder().encodeToString("mockedfile.jpg".getBytes()));
			});
		assertThat(result.getHeaders()).hasSize(4).extracting("header", "values").containsExactlyInAnyOrder(
			Tuple.tuple(MESSAGE_ID, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(IN_REPLY_TO, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(REFERENCES, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(AUTO_SUBMITTED, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
		assertThat(result.getMetadata()).isSameAs(METADATA);
	}

	@Test
	void testToEmails_receivedAtNull() throws Exception {

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
		final var result = toEmails(List.of(emailMessage), MUNICIPALITY_ID, NAMESPACE, null).getFirst();

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata", "receivedAt", "createdAt");

		assertThat(result.getSender()).isEqualTo("sender@example.com");
		assertThat(result.getRecipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.getSubject()).isEqualTo("Test Email Subject");
		assertThat(result.getMessage()).isEqualTo("Mocked email body");
		assertThat(result.getReceivedAt()).isNull();
		assertThat(result.getId()).isNotNull().isNotEmpty();
		assertThat(result.getAttachments()).hasSize(1).satisfies(
			attachment -> {
				assertThat(attachment.getFirst().getName()).isEqualTo("Mocked attachment");
				assertThat(attachment.getFirst().getContentType()).isEqualTo("text/plain");
				assertThat(attachment.getFirst().getContent()).isEqualTo(Base64.getEncoder().encodeToString("mockedfile.jpg".getBytes()));
			});
		assertThat(result.getHeaders()).hasSize(4).extracting("header", "values").containsExactlyInAnyOrder(
			Tuple.tuple(MESSAGE_ID, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(IN_REPLY_TO, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(REFERENCES, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")),
			Tuple.tuple(AUTO_SUBMITTED, List.of("<Test1@Test1.se>", "<Test2@Test2.se>", "<Test3@Test3.se>")));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getNamespace()).isEqualTo(NAMESPACE);
		assertThat(result.getMetadata()).isNull();
	}

	@Test
	void testToEmails_noHeaders_noAttachments() throws Exception {

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
		final var result = toEmails(List.of(emailMessage), MUNICIPALITY_ID, NAMESPACE, null).getFirst();

		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata", "headers", "attachments", "createdAt");

		assertThat(result.getSender()).isEqualTo("sender@example.com");
		assertThat(result.getRecipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.getSubject()).isEqualTo("Test Email Subject");
		assertThat(result.getMessage()).isEqualTo("Mocked email body");
		assertThat(result.getReceivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.getId()).isNotNull().isNotEmpty();
		assertThat(result.getAttachments()).isEmpty();
		assertThat(result.getHeaders()).hasSize(1).first().extracting("header").isEqualTo(MESSAGE_ID);
	}

	@Test
	void testToEmails_throwException() throws Exception {

		// Mock
		final var emailMessage = mock(EmailMessage.class);
		when(emailMessage.getToRecipients()).thenThrow(new ServiceLocalException("Something went wrong"));
		// Act
		final var result = toEmails(List.of(emailMessage), MUNICIPALITY_ID, NAMESPACE, METADATA);
		// Assert
		assertThat(result).isEmpty();
	}
}
