package se.sundsvall.emailreader.integration.ews;

import java.sql.Blob;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;
import se.sundsvall.emailreader.utility.BlobBuilder;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.api.model.Header.IN_REPLY_TO;
import static se.sundsvall.emailreader.api.model.Header.MESSAGE_ID;
import static se.sundsvall.emailreader.api.model.Header.REFERENCES;

@ExtendWith({
	MockitoExtension.class
})
class EWSMapperTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String NAMESPACE = "namespace";
	private static final Map<String, String> METADATA = Map.of("key", "value");

	@Mock
	private Blob blobMock;
	@Mock
	private BlobBuilder blobBuilderMock;

	@InjectMocks
	private EWSMapper ewsMapper;

	@Test
	void findHeader() {
		final var iterator = mock(Iterator.class);
		final var internetMessageHeaderCollection = mock(InternetMessageHeaderCollection.class);
		final var messageIdHeader = mock(InternetMessageHeader.class);
		when(messageIdHeader.getName()).thenReturn("message-ID");
		when(messageIdHeader.getValue()).thenReturn("<Test1@test.se>");
		final var referenceHeader = mock(InternetMessageHeader.class);
		when(referenceHeader.getName()).thenReturn("reFeRenCes");
		when(referenceHeader.getValue()).thenReturn("<Test2@test.se>");
		final var inReplyToHeader = mock(InternetMessageHeader.class);
		when(inReplyToHeader.getName()).thenReturn("in-REPLY-to");
		when(inReplyToHeader.getValue()).thenReturn("<Test3@test.se>");
		final var autoSubmittedHeader = mock(InternetMessageHeader.class);
		when(autoSubmittedHeader.getName()).thenReturn("auto-submitted");
		when(autoSubmittedHeader.getValue()).thenReturn("auto-generated");

		when(internetMessageHeaderCollection.iterator()).thenReturn(iterator);
		when(iterator.hasNext()).thenReturn(true, true, true, true, false);
		when(iterator.next()).thenReturn(messageIdHeader, referenceHeader, inReplyToHeader, autoSubmittedHeader);

		final var messageIdHeaderResult = ewsMapper.findHeader(internetMessageHeaderCollection, MESSAGE_ID);
		assertThat(messageIdHeaderResult).isPresent();
		assertThat(messageIdHeaderResult.get().getValue()).isEqualTo("<Test1@test.se>");
		final var referenceHeaderResult = ewsMapper.findHeader(internetMessageHeaderCollection, REFERENCES);
		assertThat(referenceHeaderResult).isPresent();
		assertThat(referenceHeaderResult.get().getValue()).isEqualTo("<Test2@test.se>");
		final var inReplyToHeaderResult = ewsMapper.findHeader(internetMessageHeaderCollection, IN_REPLY_TO);
		assertThat(inReplyToHeaderResult).isPresent();
		assertThat(inReplyToHeaderResult.get().getValue()).isEqualTo("<Test3@test.se>");
		final var autoSubmittedHeaderResult = ewsMapper.findHeader(internetMessageHeaderCollection, AUTO_SUBMITTED);
		assertThat(autoSubmittedHeaderResult).isPresent();
		assertThat(autoSubmittedHeaderResult.get().getValue()).isEqualTo("auto-generated");
	}

	@Test
	void toHeaders() throws ServiceLocalException {
		final var iterator = mock(Iterator.class);
		final var spy = Mockito.spy(ewsMapper);
		final var emailMessageMock = mock(EmailMessage.class);
		final var internetMessageHeaderCollection = mock(InternetMessageHeaderCollection.class);
		when(emailMessageMock.getInternetMessageHeaders()).thenReturn(internetMessageHeaderCollection);
		when(internetMessageHeaderCollection.iterator()).thenReturn(iterator);

		final var messageIdHeader = mock(InternetMessageHeader.class);
		when(messageIdHeader.getName()).thenReturn("message-ID");
		when(messageIdHeader.getValue()).thenReturn("<Test1@test.se>");
		final var referenceHeader = mock(InternetMessageHeader.class);
		when(referenceHeader.getName()).thenReturn("reFeRenCes");
		when(referenceHeader.getValue()).thenReturn("<Test2@test.se>");
		final var inReplyToHeader = mock(InternetMessageHeader.class);
		when(inReplyToHeader.getName()).thenReturn("in-REPLY-to");
		when(inReplyToHeader.getValue()).thenReturn("<Test3@test.se>");
		final var autoSubmittedHeader = mock(InternetMessageHeader.class);
		when(autoSubmittedHeader.getName()).thenReturn("auto-submitted");
		when(autoSubmittedHeader.getValue()).thenReturn("auto-generated");

		when(iterator.hasNext()).thenReturn(true, true, true, true, false);
		when(iterator.next()).thenReturn(messageIdHeader, referenceHeader, inReplyToHeader, autoSubmittedHeader);
		when(spy.findHeader(internetMessageHeaderCollection, MESSAGE_ID)).thenReturn(Optional.of(messageIdHeader));
		when(spy.findHeader(internetMessageHeaderCollection, REFERENCES)).thenReturn(Optional.of(referenceHeader));
		when(spy.findHeader(internetMessageHeaderCollection, IN_REPLY_TO)).thenReturn(Optional.of(inReplyToHeader));
		when(spy.findHeader(internetMessageHeaderCollection, AUTO_SUBMITTED)).thenReturn(Optional.of(autoSubmittedHeader));
		when(spy.createEmailHeader(any(Header.class), anyList())).thenCallRealMethod();
		when(spy.extractValues(anyString())).thenCallRealMethod();

		final var result = spy.toHeaders(emailMessageMock);

		assertThat(result).isNotNull().hasSize(4).extracting("header", "values").containsExactlyInAnyOrder(
			tuple(MESSAGE_ID, List.of("<Test1@test.se>")),
			tuple(REFERENCES, List.of("<Test2@test.se>")),
			tuple(IN_REPLY_TO, List.of("<Test3@test.se>")),
			tuple(AUTO_SUBMITTED, List.of("auto-generated")));
	}

	@Test
	void toHeadersWithWhitespace() throws ServiceLocalException {
		final var iterator = mock(Iterator.class);
		final var spy = Mockito.spy(ewsMapper);
		final var emailMessageMock = mock(EmailMessage.class);
		final var internetMessageHeaderCollection = mock(InternetMessageHeaderCollection.class);
		when(emailMessageMock.getInternetMessageHeaders()).thenReturn(internetMessageHeaderCollection);
		when(internetMessageHeaderCollection.iterator()).thenReturn(iterator);

		final var messageIdHeader = mock(InternetMessageHeader.class);
		when(messageIdHeader.getName()).thenReturn("message-ID");
		when(messageIdHeader.getValue()).thenReturn("<Test1@test.se> ");
		final var referenceHeader = mock(InternetMessageHeader.class);
		when(referenceHeader.getName()).thenReturn("reFeRenCes");
		when(referenceHeader.getValue()).thenReturn("<Test2@test.se>    \t");
		final var inReplyToHeader = mock(InternetMessageHeader.class);
		when(inReplyToHeader.getName()).thenReturn("in-REPLY-to");
		when(inReplyToHeader.getValue()).thenReturn("<Test3@test.se>     ");
		final var autoSubmittedHeader = mock(InternetMessageHeader.class);
		when(autoSubmittedHeader.getName()).thenReturn("auto-submitted");
		when(autoSubmittedHeader.getValue()).thenReturn("auto-generated ");

		when(iterator.hasNext()).thenReturn(true, true, true, true, false);
		when(iterator.next()).thenReturn(messageIdHeader, referenceHeader, inReplyToHeader, autoSubmittedHeader);
		when(spy.findHeader(internetMessageHeaderCollection, MESSAGE_ID)).thenReturn(Optional.of(messageIdHeader));
		when(spy.findHeader(internetMessageHeaderCollection, REFERENCES)).thenReturn(Optional.of(referenceHeader));
		when(spy.findHeader(internetMessageHeaderCollection, IN_REPLY_TO)).thenReturn(Optional.of(inReplyToHeader));
		when(spy.findHeader(internetMessageHeaderCollection, AUTO_SUBMITTED)).thenReturn(Optional.of(autoSubmittedHeader));
		when(spy.createEmailHeader(any(Header.class), anyList())).thenCallRealMethod();
		when(spy.extractValues(anyString())).thenCallRealMethod();

		final var result = spy.toHeaders(emailMessageMock);

		assertThat(result).isNotNull().hasSize(4).extracting("header", "values").containsExactlyInAnyOrder(
			tuple(MESSAGE_ID, List.of("<Test1@test.se>")),
			tuple(REFERENCES, List.of("<Test2@test.se>")),
			tuple(IN_REPLY_TO, List.of("<Test3@test.se>")),
			tuple(AUTO_SUBMITTED, List.of("auto-generated")));
	}

	@ParameterizedTest
	@EnumSource(Header.class)
	void createEmailHeader(final Header header) {
		final var values = List.of("<test1@test.se>");

		final var emailHeaderEntity = ewsMapper.createEmailHeader(header, values);

		assertThat(emailHeaderEntity).isNotNull().satisfies(headerEntity -> {
			assertThat(headerEntity.getHeader()).isEqualTo(header);
			assertThat(headerEntity.getValues()).containsExactly("<test1@test.se>");
		});
	}

	@Test
	void extractValues() {
		final var input = "<test1@test.se> <test2@test.se> <test3@test.se>";

		final var result = ewsMapper.extractValues(input);

		assertThat(result).isNotNull().hasSize(3)
			.containsExactlyInAnyOrder("<test1@test.se>", "<test2@test.se>", "<test3@test.se>");
	}

	@Test
	void toAttachment() {
		final var fileAttachment = mock(FileAttachment.class);
		final var contentArray = new byte[] {
			1, 2, 3
		};
		when(fileAttachment.getName()).thenReturn("test.txt");
		when(fileAttachment.getContent()).thenReturn(contentArray);
		when(blobBuilderMock.createBlob(contentArray)).thenReturn(blobMock);

		final var result = ewsMapper.toAttachment(fileAttachment);

		assertThat(result).isNotNull().satisfies(attachmentEntity -> {
			assertThat(attachmentEntity.getName()).isEqualTo("test.txt");
			assertThat(attachmentEntity.getContentType()).isEqualTo("text/plain");
			assertThat(attachmentEntity.getContent()).isEqualTo(blobMock);
		});

		verify(blobBuilderMock).createBlob(contentArray);
	}

	@Test
	void toAttachment_throws() {
		final var fileAttachment = mock(FileAttachment.class);
		final var contentArray = new byte[] {
			1, 2, 3
		};
		when(fileAttachment.getName()).thenReturn("test.txt");
		when(fileAttachment.getContent()).thenReturn(contentArray);
		when(blobBuilderMock.createBlob(contentArray)).thenThrow(new RuntimeException());

		final var result = ewsMapper.toAttachment(fileAttachment);

		assertThat(result).isNull();
		verify(blobBuilderMock).createBlob(contentArray);
	}

	@Test
	void toEmail() throws Exception {
		final var spy = Mockito.spy(ewsMapper);
		final var emailMessageMock = mock(EmailMessage.class);
		final var attachmentEntity = new AttachmentEntity();
		final var emailHeaderEntity = new EmailHeaderEntity();

		when(emailMessageMock.getId()).thenReturn(new ItemId("123456789"));
		when(emailMessageMock.getBody()).thenReturn(new MessageBody("Mocked email body"));
		when(emailMessageMock.getSubject()).thenReturn("Test Email Subject");
		when(emailMessageMock.getFrom()).thenReturn(new EmailAddress("test", "sender@example.com"));
		when(emailMessageMock.getDateTimeReceived()).thenReturn(Date.from(Instant.now()));
		when(emailMessageMock.getToRecipients()).thenReturn(new EmailAddressCollection());
		emailMessageMock.getToRecipients().add("recipient@example.com");

		when(emailMessageMock.getAttachments()).thenReturn(new AttachmentCollection());
		emailMessageMock.getAttachments().addFileAttachment("fileAttachment.txt");

		when(spy.toAttachment(any())).thenReturn(attachmentEntity);
		when(emailMessageMock.getDateTimeReceived()).thenReturn(Date.from(Instant.now()));

		when(spy.toHeaders(emailMessageMock)).thenReturn(List.of(emailHeaderEntity));

		final var result = spy.toEmail(emailMessageMock, MUNICIPALITY_ID, NAMESPACE, METADATA);

		assertThat(result).isNotNull().satisfies(emailEntity -> {
			assertThat(emailEntity).hasNoNullFieldsOrPropertiesExcept("createdAt", "id", "htmlMessage");
			assertThat(emailEntity.getOriginalId()).isEqualTo("123456789");
			assertThat(emailEntity.getSubject()).isEqualTo("Test Email Subject");
			assertThat(emailEntity.getSender()).isEqualTo("sender@example.com");
			assertThat(emailEntity.getRecipients()).hasSize(1).satisfies(
				recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
			assertThat(emailEntity.getSubject()).isEqualTo("Test Email Subject");
			assertThat(emailEntity.getMessage()).isEqualTo("Mocked email body");
			assertThat(emailEntity.getReceivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
			assertThat(emailEntity.getId()).isNull();
			assertThat(emailEntity.getHeaders()).containsExactly(emailHeaderEntity);
			assertThat(emailEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			assertThat(emailEntity.getNamespace()).isEqualTo(NAMESPACE);
			assertThat(emailEntity.getMetadata()).isNotSameAs(METADATA);
			assertThat(emailEntity.getMetadata()).allSatisfy((key, value) -> assertThat(METADATA).containsEntry(key, value));
			assertThat(emailEntity.getAttachments()).hasSize(1).containsExactly(attachmentEntity);
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
		final var result = ewsMapper.toEmail(emailMessage, MUNICIPALITY_ID, NAMESPACE, null);
		// Assert
		assertThat(result).hasNoNullFieldsOrPropertiesExcept("metadata", "headers", "attachments", "createdAt", "id", "htmlMessage");
		assertThat(result.getSender()).isEqualTo("sender@example.com");
		assertThat(result.getRecipients()).hasSize(1).satisfies(
			recipient -> assertThat(recipient.getFirst()).isEqualTo("recipient@example.com"));
		assertThat(result.getSubject()).isEqualTo("Test Email Subject");
		assertThat(result.getMessage()).isEqualTo("Mocked email body");
		assertThat(result.getReceivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.getId()).isNull();
		assertThat(result.getAttachments()).isEmpty();
		assertThat(result.getHeaders()).hasSize(1).first().extracting("header").isEqualTo(MESSAGE_ID);
	}

	@Test
	void testToEmails_throwException() throws Exception {
		// Mock
		final var emailMessage = mock(EmailMessage.class);
		when(emailMessage.getToRecipients()).thenThrow(new ServiceLocalException("Something went wrong"));
		// Act & Assert

		assertThatThrownBy(() -> ewsMapper.toEmail(emailMessage, MUNICIPALITY_ID, NAMESPACE, METADATA))
			.isInstanceOf(ServiceLocalException.class)
			.hasMessage("Something went wrong");
	}

}
