package se.sundsvall.emailreader;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;

public final class TestUtility {

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

	public static Email createEmail(final Map<Header, List<String>> headers) {

		final var headerMap = new HashMap<>(Objects.requireNonNullElse(headers, Map.of(
			Header.MESSAGE_ID, List.of("someValue"),
			Header.REFERENCES, List.of("someReferenceValue"),
			Header.IN_REPLY_TO, List.of("someReplyToValue")
		)));

		return Email.builder()
			.withId("someId")
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withMessage("someMessage")
			.withHeaders(headerMap)
			.withMetadata(Map.of("someKey", "someValue"))
			.withAttachments(List.of(Email.Attachment.builder()
				.withName("someName")
				.withContent("someContent")
				.withContentType("someContentType")
				.build()))
			.build();
	}

	public static EmailEntity createEmailEntity() {
		return EmailEntity.builder()
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender").withMessage("someMessage")
			.withId("someId")
			.withReceivedAt(OffsetDateTime.now())
			.withHeaders(List.of(
				EmailHeaderEntity.builder().withHeader(Header.MESSAGE_ID).withValues(List.of("someValue")).build(),
				EmailHeaderEntity.builder().withHeader(Header.REFERENCES).withValues(List.of("someReferenceValue")).build(),
				EmailHeaderEntity.builder().withHeader(Header.IN_REPLY_TO).withValues(List.of("someReplyToValue")).build()
			))
			.withCreatedAt(OffsetDateTime.now())
			.withNamespace("someNamespace")
			.withMetadata(Map.of("someKey", "someValue"))
			.withMunicipalityId("someMunicipalityId")
			.withAttachments(List.of(
				AttachmentEntity.builder()
					.withName("someName")
					.withContent("someContent")
					.withContentType("someContentType")
					.build()
			))
			.build();
	}

}
