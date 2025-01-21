package se.sundsvall.emailreader.service.mapper;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.emailreader.api.model.Header;

class EmailMapperTest {

	@Test
	void toEmails() {

		// Arrange
		final var entity = createEmailEntity(emptyMap());

		// Act
		final var result = EmailMapper.toEmails(List.of(entity));

		// Assert
		assertThat(result).isNotNull().hasSize(1).element(0).satisfies(email -> assertThat(result.getFirst()).isNotNull().hasNoNullFieldsOrProperties()
			.hasFieldOrPropertyWithValue("id", entity.getId())
			.hasFieldOrPropertyWithValue("subject", entity.getSubject())
			.hasFieldOrPropertyWithValue("recipients", entity.getRecipients())
			.hasFieldOrPropertyWithValue("sender", entity.getSender())
			.hasFieldOrPropertyWithValue("message", entity.getMessage())
			.hasFieldOrPropertyWithValue("receivedAt", entity.getReceivedAt())
			.hasFieldOrPropertyWithValue("metadata", entity.getMetadata()));

		assertThat(result.getFirst().attachments()).hasSize(1).element(0).satisfies(attachment -> assertThat(attachment).isNotNull().hasNoNullFieldsOrPropertiesExcept("content")
			.hasFieldOrPropertyWithValue("name", entity.getAttachments().getFirst().getName())
			.hasFieldOrPropertyWithValue("contentType", entity.getAttachments().getFirst().getContentType()));

		assertThat(result.getFirst().headers()).hasSize(3).contains(
			new SimpleEntry<>(Header.MESSAGE_ID, List.of("someValue")),
			new SimpleEntry<>(Header.REFERENCES, List.of("someReferenceValue")),
			new SimpleEntry<>(Header.IN_REPLY_TO, List.of("someReplyToValue")));
	}

	@Test
	void toEmail() {

		// Arrange
		final var entity = createEmailEntity(emptyMap());

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

		assertThat(result.attachments()).hasSize(1).element(0).satisfies(attachment -> assertThat(attachment).isNotNull().hasNoNullFieldsOrPropertiesExcept("content")
			.hasFieldOrPropertyWithValue("name", entity.getAttachments().getFirst().getName())
			.hasFieldOrPropertyWithValue("contentType", entity.getAttachments().getFirst().getContentType()));

		assertThat(result.headers()).hasSize(3).contains(
			new SimpleEntry<>(Header.MESSAGE_ID, List.of("someValue")),
			new SimpleEntry<>(Header.REFERENCES, List.of("someReferenceValue")),
			new SimpleEntry<>(Header.IN_REPLY_TO, List.of("someReplyToValue")));
	}

	@Test
	void toAttachment() {

		// Arrange
		final var attachmentEntity = createEmailEntity(emptyMap()).getAttachments().getFirst();

		// Act
		final var result = EmailMapper.toAttachment(attachmentEntity);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("content")
			.hasFieldOrPropertyWithValue("name", attachmentEntity.getName())
			.hasFieldOrPropertyWithValue("contentType", attachmentEntity.getContentType());
	}
}
