package se.sundsvall.emailreader.integration.ews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.EmailAddressCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

@ExtendWith({MockitoExtension.class})
class EWSMapperTest {

	@InjectMocks
	private EWSMapper mapper;

	@Test
	void testToEmail() throws Exception {

		final EmailMessage emailMessage = mock(EmailMessage.class);

		when(emailMessage.getToRecipients()).thenReturn(new EmailAddressCollection());
		emailMessage.getToRecipients().add("recipient@example.com");

		when(emailMessage.getId()).thenReturn(new ItemId("123456789"));
		when(emailMessage.getBody()).thenReturn(new MessageBody("Mocked email body"));
		when(emailMessage.getSubject()).thenReturn("Test Email Subject");
		when(emailMessage.getFrom()).thenReturn(new EmailAddress("test", "sender@example.com"));

		when(emailMessage.getAttachments()).thenReturn(new AttachmentCollection());
		final AttachmentCollection attachments = emailMessage.getAttachments();
		final FileAttachment fileAttachment = mock(FileAttachment.class);
		when(fileAttachment.getName()).thenReturn("Mocked attachment");
		when(fileAttachment.getContent()).thenReturn("mockedfile.jpg".getBytes());
		when(fileAttachment.getContentType()).thenReturn("text/plain");
		attachments.getItems().add(fileAttachment);

		final var result = mapper.toEmail(emailMessage);

		assertThat(result.from()).isEqualTo("sender@example.com");
		assertThat(result.to()).hasSize(1).satisfies(
			to -> assertThat(to.get(0)).isEqualTo("recipient@example.com"));
		assertThat(result.subject()).isEqualTo("Test Email Subject");
		assertThat(result.message()).isEqualTo("Mocked email body");
		assertThat(result.id()).isNotNull().isNotEmpty();
		assertThat(result.attachments()).hasSize(1).satisfies(
			attachment -> {
				assertThat(attachment.get(0).name()).isEqualTo("Mocked attachment");
				assertThat(attachment.get(0).contentType()).isEqualTo("text/plain");
				assertThat(attachment.get(0).content()).isEqualTo(Base64.getEncoder().encodeToString("mockedfile.jpg".getBytes()));
			});
	}

}
