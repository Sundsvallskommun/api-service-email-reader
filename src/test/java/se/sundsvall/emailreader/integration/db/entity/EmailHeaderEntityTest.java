package se.sundsvall.emailreader.integration.db.entity;

import java.util.List;
import java.util.UUID;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.emailreader.api.model.Header;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class EmailHeaderEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmailHeaderEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilder() {

		// Arrange
		final var id = UUID.randomUUID().toString();
		final var header = Header.IN_REPLY_TO;
		final var values = List.of("someValue", "someOtherValue");

		// Act
		final var result = EmailHeaderEntity.builder()
			.withId(id)
			.withHeader(header)
			.withValues(values)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getHeader()).isEqualTo(header);
		assertThat(result.getValues()).isEqualTo(values);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new EmailHeaderEntity()).hasAllNullFieldsOrProperties();
		assertThat(EmailHeaderEntity.builder().build()).hasAllNullFieldsOrProperties();
	}

}
