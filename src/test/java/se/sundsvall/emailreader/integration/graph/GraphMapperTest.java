package se.sundsvall.emailreader.integration.graph;

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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.utility.BlobBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmailHeaderEntity;
import static se.sundsvall.emailreader.TestUtility.createMessageWithHeaders;

@ExtendWith(MockitoExtension.class)
class GraphMapperTest {

	@Mock
	private BlobBuilder blobBuilder;

	@Mock
	private Blob blob;

	@InjectMocks
	private GraphMapper graphMapper;

	private static Stream<Arguments> findHeaderArgumentProvider() {
		return Stream.of(
			Arguments.of(Header.REFERENCES, "ref1, ref2"),
			Arguments.of(Header.MESSAGE_ID, "12345"),
			Arguments.of(Header.IN_REPLY_TO, "reply1"),
			Arguments.of(Header.AUTO_SUBMITTED, "auto"));
	}

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
		final var message = createMessage();
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var metadata = Map.of("key", "value");
		final var recipients = List.of("recipient@example.com");
		final var sender = "sender@example.com";
		final var messageIdHeader = createEmailHeaderEntity(Header.MESSAGE_ID, List.of("message-id"));
		final var headers = List.of(messageIdHeader);
		final var messageContent = "messageContent";

		final var spy = Mockito.spy(graphMapper);

		when(spy.getRecipients(message)).thenReturn(recipients);
		when(spy.getSender(message)).thenReturn(sender);
		when(spy.toHeaders(message)).thenReturn(headers);
		when(spy.stripHTML(message)).thenReturn(messageContent);

		final var result = spy.toEmail(message, municipalityId, namespace, metadata);

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
		final var message = createMessageWithHeaders();

		final var spy = Mockito.spy(graphMapper);
		when(spy.createEmailHeader(any(), any())).thenCallRealMethod();

		final var result = spy.toHeaders(message);

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
		final var message = createMessageWithHeaders();

		final var result = graphMapper.findHeader(message, header.getName());

		assertThat(result)
			.isPresent()
			.contains(value);
	}

	@ParameterizedTest
	@EnumSource(Header.class)
	void createEmailHeader(final Header header) {
		final var values = List.of("value1", "value2");

		final var result = graphMapper.createEmailHeader(header, values);

		assertThat(result).isNotNull().satisfies(emailHeader -> {
			assertThat(emailHeader.getHeader()).isEqualTo(header);
			assertThat(emailHeader.getValues()).isEqualTo(values);
		});
	}

	@Test
	void stripHTML() {
		final var message = createMessage();
		final var body = new ItemBody();
		body.setContent("<html><body><p>This is a <strong>test</strong> message with <em>HTML</em> tags.</p></body></html>");
		message.setBody(body);

		final var result = graphMapper.stripHTML(message);

		assertThat(result).isNotNull().isEqualTo("This is a test message with HTML tags.");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"<html><head>\u2028<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><style type=\"text/css\" style=\"display:none\">\u2028<!--\u2028p\u2028\t{margin-top:0;\u2028\tmargin-bottom:0}\u2028-->\u2028</style></head><body dir=\"ltr\"><div id=\"divtagdefaultwrapper\" dir=\"ltr\" style=\"font-size:12pt; color:#000000; font-family:Calibri,Helvetica,sans-serif\"><p></p><div>Hej.<br><br>En rad<br>En rad under den<br><br>En rad med dubbla radbryt.</div><p></p></div></body></html>\n",
		"""
			<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
			<meta name="Generator" content="Microsoft Exchange Server"><!-- converted from text --><style><!-- .EmailQuote { margin-left: 1pt; padding-left: 4pt; border-left: #800000 2px solid; } --></style></head>
			<body>
			<font size="2"><span style="font-size:11pt;">
			<div class="PlainText">Hej.<br><br>En rad<br>En rad under den<br><br>En rad med dubbla radbryt.&nbsp; <br></div></span></font>
			</body>
			</html>
			""",
		"<html><head>\u2028<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><meta name=\"Generator\" content=\"Microsoft Word 15 (filtered medium)\"><style>\u2028<!--\u2028@font-face\u2028\t{font-family:\"Cambria Math\"}\u2028@font-face\u2028\t{font-family:Aptos}\u2028p.MsoNormal, li.MsoNormal, div.MsoNormal\u2028\t{margin:0in;\u2028\tfont-size:12.0pt;\u2028\tfont-family:\"Aptos\",sans-serif}\u2028span.EmailStyle17\u2028\t{font-family:\"Aptos\",sans-serif;\u2028\tcolor:windowtext}\u2028.MsoChpDefault\u2028\t{}\u2028@page WordSection1\u2028\t{margin:1.0in 1.0in 1.0in 1.0in}\u2028div.WordSection1\u2028\t{}\u2028-->\u2028</style></head><body lang=\"EN-US\" link=\"#467886\" vlink=\"#96607D\" style=\"word-wrap:break-word\"><div class=\"WordSection1\"><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">Hej.</span></p><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">&nbsp;</span></p><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">En rad</span></p><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">En rad under den</span></p><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">&nbsp;</span></p><p class=\"MsoNormal\"><span lang=\"SV\" style=\"font-size:11.0pt\">En rad med dubbla radbryt.</span></p></div></body></html>\n"

	})
	void stripHTMLCleaned(final String content) {
		final var message = createMessage();
		final var body = new ItemBody();

		body.setContent(content);
		message.setBody(body);

		// Act
		final var result = graphMapper.stripHTML(message);

		// Assert
		assertThat(result).isNotNull()
			.isEqualTo("""
				Hej.

				En rad
				En rad under den

				En rad med dubbla radbryt.""");
	}

	@Test
	void getSender() {
		final var message = createMessage();

		final var result = graphMapper.getSender(message);

		assertThat(result).isNotNull().isEqualTo(message.getSender().getEmailAddress().getAddress());
	}

	@Test
	void extractValues() {
		final var input = "value1 value2 value3";

		final var result = graphMapper.extractValues(input);

		assertThat(result).isNotNull().hasSize(3).containsExactly("value1", "value2", "value3");
	}

	@Test
	void getRecipients() {
		final var message = createMessage();

		final var result = graphMapper.getRecipients(message);

		assertThat(result).isNotNull().hasSameSizeAs(message.getToRecipients());
		assertThat(result.getFirst()).isEqualTo(message.getToRecipients().getFirst().getEmailAddress().getAddress());
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
