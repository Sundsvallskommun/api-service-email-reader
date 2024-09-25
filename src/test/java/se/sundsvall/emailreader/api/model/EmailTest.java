package se.sundsvall.emailreader.api.model;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class EmailTest {

	@Test
	void testBuilderMethods() {

		final var result = Email.builder()
			.withId("someId")
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withMessage("someMessage")
			.withReceivedAt(OffsetDateTime.now())
			.withMetadata(Map.of("someKey", "someValue"))
			.withAttachments(List.of(Email.Attachment.builder()
				.withName("someName")
				.withContent("someContent")
				.withContentType("someContentType")
				.build()))
			.build();

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("someId");
		assertThat(result.subject()).isEqualTo("someSubject");
		assertThat(result.recipients()).hasSize(1).element(0).satisfies(recipient -> assertThat(recipient).isEqualTo("someRecipient"));
		assertThat(result.sender()).isEqualTo("someSender");
		assertThat(result.message()).isEqualTo("someMessage");
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.receivedAt()).isNotNull().isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment.name()).isEqualTo("someName");
			assertThat(attachment.contentType()).isEqualTo("someContentType");
			assertThat(attachment.content()).isEqualTo("someContent");
		});

	}
}
