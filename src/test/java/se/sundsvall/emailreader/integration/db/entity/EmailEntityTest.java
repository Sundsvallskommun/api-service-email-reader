package se.sundsvall.emailreader.integration.db.entity;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.code.beanmatchers.BeanMatchers;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class EmailEntityTest {

	@Test
	void testBean() {

		BeanMatchers.registerValueGenerator(LocalDateTime::now, LocalDateTime.class);
		BeanMatchers.registerValueGenerator(OffsetDateTime::now, OffsetDateTime.class);

		MatcherAssert.assertThat(EmailEntity.class, allOf(
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
			.withTo(List.of("someTo"))
			.withFrom("someFrom")
			.withSubject("someSubject")
			.withMessage("someMessage")
			.withReceivedAt(OffsetDateTime.now())
			.withCreatedAt(LocalDateTime.now())
			.withMetadata(Map.of("someKey", "someValue"))
			.withAttachments(List.of(AttachmentEntity.builder()
				.withName("someName")
				.withContent("someContent")
				.withContentType("someContentType")
				.build())).build();

		object.prePersist();
		assertThat(object.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
		assertThat(object.getReceivedAt()).isCloseTo(OffsetDateTime.now(), within(1, SECONDS));
		Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}

}
