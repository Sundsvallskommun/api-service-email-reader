package se.sundsvall.emailreader.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.emailreader.TestUtility.createEmail;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.entity.EmailHeaderEntity;

class EmailMapperTest {

	@Test
	void toEmails() {

		// Arrange
		final var entity = createEmailEntity();
		// Act
		final var result = EmailMapper.toEmails(List.of(entity));

		// Assert
		assertThat(result).isNotNull().hasSize(1).element(0).satisfies(email ->
			assertThat(result.getFirst()).isNotNull().hasNoNullFieldsOrProperties()
				.hasFieldOrPropertyWithValue("id", entity.getId())
				.hasFieldOrPropertyWithValue("subject", entity.getSubject())
				.hasFieldOrPropertyWithValue("recipients", entity.getRecipients())
				.hasFieldOrPropertyWithValue("sender", entity.getSender())
				.hasFieldOrPropertyWithValue("message", entity.getMessage())
				.hasFieldOrPropertyWithValue("receivedAt", entity.getReceivedAt())
				.hasFieldOrPropertyWithValue("metadata", entity.getMetadata()));

		assertThat(result.getFirst().attachments()).hasSize(1).element(0).satisfies(attachment ->
			assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties()
				.hasFieldOrPropertyWithValue("name", entity.getAttachments().getFirst().getName())
				.hasFieldOrPropertyWithValue("contentType", entity.getAttachments().getFirst().getContentType())
				.hasFieldOrPropertyWithValue("content", entity.getAttachments().getFirst().getContent())
		);

		assertThat(result.getFirst().headers()).hasSize(3).contains(
			new SimpleEntry<>(Header.MESSAGE_ID, List.of("someValue")),
			new SimpleEntry<>(Header.REFERENCES, List.of("someReferenceValue")),
			new SimpleEntry<>(Header.IN_REPLY_TO, List.of("someReplyToValue"))
		);
	}

	@Test
	void toEmail() {

		// Arrange
		final var entity = createEmailEntity();
		// Act
		final var result = EmailMapper.toEmail(entity);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties()
			.satisfies(email -> assertThat(email)
				.hasFieldOrPropertyWithValue("id", entity.getId())
				.hasFieldOrPropertyWithValue("subject", entity.getSubject())
				.hasFieldOrPropertyWithValue("recipients", entity.getRecipients())
				.hasFieldOrPropertyWithValue("sender", entity.getSender())
				.hasFieldOrPropertyWithValue("message", entity.getMessage())
				.hasFieldOrPropertyWithValue("receivedAt", entity.getReceivedAt())
				.hasFieldOrPropertyWithValue("metadata", entity.getMetadata()));

		assertThat(result.attachments()).hasSize(1).element(0).satisfies(attachment ->
			assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties()
				.hasFieldOrPropertyWithValue("name", entity.getAttachments().getFirst().getName())
				.hasFieldOrPropertyWithValue("contentType", entity.getAttachments().getFirst().getContentType())
				.hasFieldOrPropertyWithValue("content", entity.getAttachments().getFirst().getContent())
		);

		assertThat(result.headers()).hasSize(3).contains(
			new SimpleEntry<>(Header.MESSAGE_ID, List.of("someValue")),
			new SimpleEntry<>(Header.REFERENCES, List.of("someReferenceValue")),
			new SimpleEntry<>(Header.IN_REPLY_TO, List.of("someReplyToValue"))
		);
	}

	@Test
	void toEmailEntity() {

		// Arrange
		final var email = createEmail(null);
		final var metadata = Map.of("someKey", "someValue");
		// Act
		final var result = EmailMapper.toEmailEntity(email, "someNamespace", "someMunicipalityId", metadata);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "receivedAt", "createdAt")
			.satisfies(entity -> assertThat(entity)
				.hasFieldOrPropertyWithValue("subject", email.subject())
				.hasFieldOrPropertyWithValue("recipients", email.recipients())
				.hasFieldOrPropertyWithValue("sender", email.sender())
				.hasFieldOrPropertyWithValue("message", email.message())
				.hasFieldOrPropertyWithValue("metadata", metadata));

		assertThat(result.getAttachments()).hasSize(1).element(0).satisfies(attachment ->
			assertThat(attachment).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "createdAt")
				.hasFieldOrPropertyWithValue("name", email.attachments().getFirst().name())
				.hasFieldOrPropertyWithValue("contentType", email.attachments().getFirst().contentType())
				.hasFieldOrPropertyWithValue("content", email.attachments().getFirst().content())
		);

		assertThat(result.getHeaders()).hasSize(3).contains(
			EmailHeaderEntity.builder()
				.withHeader(Header.MESSAGE_ID)
				.withValues(List.of("someValue"))
				.build(),
			EmailHeaderEntity.builder()
				.withHeader(Header.REFERENCES)
				.withValues(List.of("someReferenceValue"))
				.build(),
			EmailHeaderEntity.builder()
				.withHeader(Header.IN_REPLY_TO)
				.withValues(List.of("someReplyToValue"))
				.build()
		);
	}

	@Test
	void toAttachment() {

		// Arrange
		final var attachmentEntity = createEmailEntity().getAttachments().getFirst();
		// Act
		final var result = EmailMapper.toAttachment(attachmentEntity);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties()
			.hasFieldOrPropertyWithValue("name", attachmentEntity.getName())
			.hasFieldOrPropertyWithValue("contentType", attachmentEntity.getContentType())
			.hasFieldOrPropertyWithValue("content", attachmentEntity.getContent());
	}

}
