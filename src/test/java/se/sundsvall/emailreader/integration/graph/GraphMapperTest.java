package se.sundsvall.emailreader.integration.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmailHeaderEntity;
import static se.sundsvall.emailreader.TestUtility.createMessageWithHeaders;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import java.sql.Blob;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.utility.BlobBuilder;

@ExtendWith(MockitoExtension.class)
class GraphMapperTest {

	@Mock
	private BlobBuilder blobBuilder;

	@Mock
	private Blob blob;

	@InjectMocks
	private GraphMapper graphMapper;

	@Test
	void testToEmails() {
		// Arrange
		final var message = createMessage();
		final var messages = List.of(message);
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var metadata = Map.of("key", "value");

		// Act
		final var emailEntities = graphMapper.toEmails(messages, municipalityId, namespace, metadata);

		// Assert
		assertThat(emailEntities).isNotNull().hasSize(1);
		final var emailEntity = emailEntities.getFirst();
		assertThat(emailEntity.getOriginalId()).isEqualTo(message.getId());
		assertThat(emailEntity.getNamespace()).isEqualTo(namespace);
		assertThat(emailEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(emailEntity.getMetadata()).isEqualTo(metadata);
	}

	@Test
	void toAttachments() {

		// Arrange
		final var attachments = createAttachments();

		when(blobBuilder.createBlob(any())).thenReturn(blob);
		// Act
		final var result = graphMapper.toAttachments(attachments);
		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getName()).isEqualTo("test.txt");
		assertThat(result.getFirst().getContentType()).isEqualTo("text/plain");
		assertThat(result.getFirst().getContent()).isEqualTo(blob);
	}

	@Test
	void toEmail() {
		var message = createMessage();
		var municipalityId = "municipalityId";
		var namespace = "namespace";
		var metadata = Map.of("key", "value");
		var recipients = List.of("recipient@example.com");
		var sender = "sender@example.com";
		var messageIdHeader = createEmailHeaderEntity(Header.MESSAGE_ID, List.of("message-id"));
		var headers = List.of(messageIdHeader);
		var messageContent = "messageContent";

		var spy = Mockito.spy(graphMapper);

		when(spy.getRecipients(message)).thenReturn(recipients);
		when(spy.getSender(message)).thenReturn(sender);
		when(spy.toHeaders(message)).thenReturn(headers);
		when(spy.getMessage(message)).thenReturn(messageContent);

		var result = spy.toEmail(message, municipalityId, namespace, metadata);

		assertThat(result).isNotNull().satisfies(emailEntity -> {
			assertThat(emailEntity.getOriginalId()).isEqualTo(message.getId());
			assertThat(emailEntity.getNamespace()).isEqualTo(namespace);
			assertThat(emailEntity.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(emailEntity.getRecipients()).isEqualTo(recipients);
			assertThat(emailEntity.getSender()).isEqualTo(sender);
			assertThat(emailEntity.getSubject()).isEqualTo(message.getSubject());
			assertThat(emailEntity.getHeaders()).containsExactly(messageIdHeader);
			assertThat(emailEntity.getMessage()).isEqualTo(messageContent);
			assertThat(emailEntity.getReceivedAt()).isEqualTo(message.getReceivedDateTime());
			assertThat(emailEntity.getMetadata()).isEqualTo(metadata);
		});
	}

	@Test
	void toHeaders() {
		var message = createMessageWithHeaders();

		var spy = Mockito.spy(graphMapper);
		when(spy.createEmailHeader(any(), any())).thenCallRealMethod();

		var result = spy.toHeaders(message);

		assertThat(result).extracting("header", "values").containsExactlyInAnyOrder(
			tuple(Header.MESSAGE_ID, List.of("12345")),
			tuple(Header.REFERENCES, List.of("ref1,", "ref2")),
			tuple(Header.IN_REPLY_TO, List.of("reply1")),
			tuple(Header.AUTO_SUBMITTED, List.of("auto")));

		verify(spy, times(4)).createEmailHeader(any(), any());
		verify(spy).toHeaders(any());
	}

	@ParameterizedTest
	@MethodSource("findHeaderArgumentProvider")
	void findHeader(final Header header, final String value) {
		var message = createMessageWithHeaders();

		var result = graphMapper.findHeader(message, header.getName());

		assertThat(result)
			.isPresent()
			.contains(value);
	}

	@ParameterizedTest
	@EnumSource(Header.class)
	void createEmailHeader(final Header header) {
		var values = List.of("value1", "value2");

		var result = graphMapper.createEmailHeader(header, values);

		assertThat(result).isNotNull().satisfies(emailHeader -> {
			assertThat(emailHeader.getHeader()).isEqualTo(header);
			assertThat(emailHeader.getValues()).isEqualTo(values);
		});
	}

	@Test
	void getMessage() {
		var message = createMessage();

		var result = graphMapper.getMessage(message);

		assertThat(result).isNotNull().isEqualTo(message.getBody().getContent());
	}

	@Test
	void getSender() {
		var message = createMessage();

		var result = graphMapper.getSender(message);

		assertThat(result).isNotNull().isEqualTo(message.getSender().getEmailAddress().getAddress());
	}

	@Test
	void extractValues() {
		var input = "value1 value2 value3";

		var result = graphMapper.extractValues(input);

		assertThat(result).isNotNull().hasSize(3).containsExactly("value1", "value2", "value3");
	}

	@Test
	void getRecipients() {
		var message = createMessage();

		var result = graphMapper.getRecipients(message);

		assertThat(result).isNotNull().hasSameSizeAs(message.getToRecipients());
		assertThat(result.getFirst()).isEqualTo(message.getToRecipients().getFirst().getEmailAddress().getAddress());
	}

	private static Stream<Arguments> findHeaderArgumentProvider() {
		return Stream.of(
			Arguments.of(Header.REFERENCES, "ref1, ref2"),
			Arguments.of(Header.MESSAGE_ID, "12345"),
			Arguments.of(Header.IN_REPLY_TO, "reply1"),
			Arguments.of(Header.AUTO_SUBMITTED, "auto"));
	}

	private Message createMessage() {
		final var message = new Message();
		message.setId(UUID.randomUUID().toString());
		message.setSubject("Test Subject");
		message.setReceivedDateTime(OffsetDateTime.now());

		final var body = new ItemBody();
		body.setContent("Test Content");
		message.setBody(body);

		final var senderEmail = new EmailAddress();
		senderEmail.setAddress("sender@example.com");
		final var sender = new Recipient();
		sender.setEmailAddress(senderEmail);
		message.setSender(sender);

		final var recipientEmail = new EmailAddress();
		recipientEmail.setAddress("recipient@example.com");
		final var recipient = new Recipient();
		recipient.setEmailAddress(recipientEmail);
		message.setToRecipients(List.of(recipient));

		final var header = new InternetMessageHeader();
		header.setName("Message-ID");
		header.setValue("<message-id@example.com>");
		message.setInternetMessageHeaders(List.of(header));

		return message;
	}

	private List<Attachment> createAttachments() {
		final var attachment = new FileAttachment();
		attachment.setName("test.txt");
		attachment.setContentBytes("Test Content".getBytes());
		attachment.setContentType("text/plain");

		return List.of(attachment);
	}

}
