package se.sundsvall.emailreader;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

public class TestUtility {


	public static Credentials createCredentials() {
		return createCredentialsWithPassword(null);

	}

	public static Credentials createCredentialsWithPassword(final String password) {
		return Credentials.builder()
			.withDestinationFolder("someDestinationFolder")
			.withDomain("someDomain")
			.withNamespace("someNamespace")
			.withMunicipalityId("someMunicipalityId")
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

	public static Email createEmail() {

		return Email.builder()
			.withId("someId")
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withMessage("someMessage")
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
			.withCreatedAt(LocalDateTime.now())
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
