package se.sundsvall.emailreader.integration.db.entity;

import java.time.LocalDateTime;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class AttachmentEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {

		assertThat(AttachmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		// Arrange
		final var content = "content";
		final var file = new MariaDbBlob(content.getBytes());
		final var id = 1L;
		final var name = "someName";
		final var contentType = "someContentType";
		final var now = now();

		final var object = AttachmentEntity.builder()
			.withId(id)
			.withName(name)
			.withContent(file)
			.withContentType(contentType)
			.build();

		// Act
		object.prePersist();

		// Assert
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(object.getId()).isEqualTo(id);
		assertThat(object.getName()).isEqualTo(name);
		assertThat(object.getContent()).isEqualTo(file);
		assertThat(object.getContentType()).isEqualTo(contentType);
		assertThat(object.getCreatedAt()).isCloseTo(now, within(1, SECONDS));

	}

}
