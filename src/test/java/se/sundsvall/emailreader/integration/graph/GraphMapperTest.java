package se.sundsvall.emailreader.integration.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

	private Message createMessage() {
		final var message = new Message();
		message.setId(UUID.randomUUID().toString());
		message.setSubject("Test Subject");
		message.setReceivedDateTime(OffsetDateTime.now());
		message.setCreatedDateTime(OffsetDateTime.now());

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
