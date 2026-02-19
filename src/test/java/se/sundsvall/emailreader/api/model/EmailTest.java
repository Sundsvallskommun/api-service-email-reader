package se.sundsvall.emailreader.api.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EmailTest {

	@Test
	void testBuilderMethods() {

		final var result = Email.builder()
			.withId("someId")
			.withSubject("someSubject")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withMessage("someMessage")
			.withHtmlMessage("someHtmlMessage")
			.withReceivedAt(OffsetDateTime.now())
			.withMetadata(Map.of("someKey", "someValue"))
			.withAttachments(List.of(Email.Attachment.builder()
				.withId(1L)
				.withName("someName")
				.withContentType("someContentType")
				.build()))
			.build();

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo("someId");
		assertThat(result.subject()).isEqualTo("someSubject");
		assertThat(result.recipients()).hasSize(1).element(0).satisfies(recipient -> assertThat(recipient).isEqualTo("someRecipient"));
		assertThat(result.sender()).isEqualTo("someSender");
		assertThat(result.message()).isEqualTo("someMessage");
		assertThat(result.htmlMessage()).isEqualTo("someHtmlMessage");
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.receivedAt()).isNotNull().isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		assertThat(result.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment).isNotNull();
			assertThat(attachment.id()).isEqualTo(1);
			assertThat(attachment.name()).isEqualTo("someName");
			assertThat(attachment.contentType()).isEqualTo("someContentType");
		});

	}
}
