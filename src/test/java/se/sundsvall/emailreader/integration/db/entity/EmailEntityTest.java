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
	void testFields() {

		final var object = EmailEntity.builder()
			.withId(String.valueOf(new Random().nextInt()))
			.withNamespace("someNamespace")
			.withMunicipalityId("someMunicipalityId")
			.withRecipients(List.of("someRecipient"))
			.withSender("someSender")
			.withSubject("someSubject")
			.withMessage("someMessage")
			.withReceivedAt(now())
			.withCreatedAt(now())
			.withMetadata(Map.of("someKey", "someValue"))
			.withAttachments(List.of(AttachmentEntity.builder()
				.withName("someName")
				.withContent("someContent")
				.withContentType("someContentType")
				.build())).build();

		object.prePersist();
		assertThat(object.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
		assertThat(object.getReceivedAt()).isCloseTo(now(), within(1, SECONDS));
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}

}
