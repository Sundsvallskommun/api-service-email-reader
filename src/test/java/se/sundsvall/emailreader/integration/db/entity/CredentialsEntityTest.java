package se.sundsvall.emailreader.integration.db.entity;

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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CredentialsEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), LocalDateTime.class);
	}

	@Test
	void testBean() {

		assertThat(CredentialsEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {

		final var object = CredentialsEntity.builder()
			.withId(String.valueOf(new Random().nextInt()))
			.withUsername("someUsername")
			.withPassword("somePassword")
			.withDomain("someDomain")
			.withMunicipalityId("someMunicipalityId")
			.withEmailAddress(Collections.singletonList("someEmailAddress"))
			.withNamespace("someNamespace")
			.withDestinationFolder("someDestinationFolder")
			.withCreatedAt(LocalDateTime.now())
			.withMetadata(Map.of("someKey", "someValue"))
			.withAction("PERSIST")
			.build();

		object.prePersist();
		assertThat(object.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
		assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}

}
