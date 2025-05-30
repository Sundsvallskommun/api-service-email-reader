package se.sundsvall.emailreader;

import com.microsoft.graph.models.InternetMessageHeader;
import com.microsoft.graph.models.Message;
import java.sql.Blob;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.mariadb.jdbc.MariaDbBlob;
import org.mockito.Mock;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;

public final class TestUtility {

	@Mock
	private Blob blobMock;

	public static Credentials createCredentials() {
		return createCredentialsWithPassword(null);
	}

	public static Credentials createCredentialsWithPassword(final String password) {
		return Credentials.builder()
			.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withUsername("someUsername")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withId("someId")
			.withMetadata(Map.of("someKey", "someValue"))
			.withPassword(password)
			.build();
	}

	public static CredentialsEntity createCredentialsEntity() {
		return CredentialsEntity.builder()
			.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withMunicipalityId("someMunicipalityId")
			.withUsername("someUsername")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withId("someId")
			.withPassword("somePassword")
			.withMetadata(Map.of("someKey", "someValue"))
			.build();
	}

	public static EmailEntity createEmailEntity(final Map<Header, List<String>> headers) {

		final var headerList = new ArrayList<EmailHeaderEntity>();
		if (headers.isEmpty()) {
			headerList.add(EmailHeaderEntity.builder().withHeader(Header.MESSAGE_ID).withValues(List.of("someValue")).build());
			headerList.add(EmailHeaderEntity.builder().withHeader(Header.REFERENCES).withValues(List.of("someReferenceValue")).build());
			headerList.add(EmailHeaderEntity.builder().withHeader(Header.IN_REPLY_TO).withValues(List.of("someReplyToValue")).build());
		} else {
			headers.forEach((key, value) -> headerList.add(EmailHeaderEntity.builder().withHeader(key).withValues(value).build()));
		}

		final var content = "content";
		final var file = new MariaDbBlob(content.getBytes());

		return EmailEntity.builder()
			.withId("someId")
			.withOriginalId("someOriginalId")
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withMessage("someMessage")
			.withHeaders(headerList)
			.withMetadata(Map.of("someKey", "someValue"))
			.withNamespace("someNamespace")
			.withMunicipalityId("someMunicipalityId")
			.withCreatedAt(OffsetDateTime.now())
			.withReceivedAt(OffsetDateTime.now())
			.withAttachments(List.of(AttachmentEntity.builder()
				.withId(1L)
				.withName("someName")
				.withContent(file)
				.withContentType("someContentType")
				.build()))
			.build();
	}

	public static EmailHeaderEntity createEmailHeaderEntity(Header header, List<String> values) {
		return EmailHeaderEntity.builder()
			.withHeader(header)
			.withValues(values)
			.build();
	}

	public static Message createMessageWithHeaders() {
		var message = new Message();
		var messageIdHeader = new InternetMessageHeader();
		messageIdHeader.setName("message-id");
		messageIdHeader.setValue("12345");
		var referencesHeader = new InternetMessageHeader();
		referencesHeader.setName("references");
		referencesHeader.setValue("ref1, ref2");
		var inReplyToHeader = new InternetMessageHeader();
		inReplyToHeader.setName("in-reply-to");
		inReplyToHeader.setValue("reply1");
		var autoSubmittedHeader = new InternetMessageHeader();
		autoSubmittedHeader.setName("auto-submitted");
		autoSubmittedHeader.setValue("auto");
		message.setInternetMessageHeaders(List.of(messageIdHeader, referencesHeader, inReplyToHeader, autoSubmittedHeader));
		return message;
	}

}
