package se.sundsvall.emailreader.integration.db.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.emailreader.api.model.Header;

class EmailEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {

		assertThat(EmailEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilder() {
		// ARRANGE
		final var id = String.valueOf(new Random().nextInt());
		final var namespace = "someNamespace";
		final var municipalityId = "someMunicipalityId";
		final var recipients = List.of("someRecipient");
		final var sender = "someSender";
		final var subject = "someSubject";
		final var headers = List.of(EmailHeaderEntity.builder()
			.withHeader(Header.IN_REPLY_TO)
			.withValues(List.of("someValue"))
			.build());
		final var message = "someMessage";
		final var receivedAt = now();
		final var createdAt = now();
		final var metadata = Map.of("someKey", "someValue");
		final var attachments = List.of(AttachmentEntity.builder()
			.withName("someName")
			.withContent("someContent")
			.withContentType("someContentType")
			.build());

		// ACT
		final var object = EmailEntity.builder()
			.withId(id)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withRecipients(recipients)
			.withSender(sender)
			.withSubject(subject)
			.withHeaders(headers)
			.withMessage(message)
			.withReceivedAt(receivedAt)
			.withCreatedAt(createdAt)
			.withMetadata(metadata)
			.withAttachments(attachments)
			.build();

		object.prePersist();

		// ASSERT
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(object.getId()).isEqualTo(id);
		assertThat(object.getNamespace()).isEqualTo(namespace);
		assertThat(object.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(object.getRecipients()).isEqualTo(recipients);
		assertThat(object.getSender()).isEqualTo(sender);
		assertThat(object.getSubject()).isEqualTo(subject);
		assertThat(object.getHeaders()).isEqualTo(headers);
		assertThat(object.getMessage()).isEqualTo(message);
		assertThat(object.getReceivedAt()).isCloseTo(receivedAt, within(1, SECONDS));
		assertThat(object.getCreatedAt()).isCloseTo(createdAt, within(1, SECONDS));
		assertThat(object.getMetadata()).isEqualTo(metadata);
		assertThat(object.getAttachments()).isEqualTo(attachments);
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new EmailEntity()).hasAllNullFieldsOrProperties();
		assertThat(EmailEntity.builder().build()).hasAllNullFieldsOrProperties();
	}

}
