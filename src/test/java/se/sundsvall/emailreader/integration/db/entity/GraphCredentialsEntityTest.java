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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GraphCredentialsEntityTest {
	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(GraphCredentialsEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var id = "someId";
		final var municipalityId = "someMunicipalityId";
		final var destinationFolder = "someDestinationFolder";
		final var clientId = "someClientId";
		final var clientSecret = "someClientSecret";
		final var tenantId = "someTenantId";
		final var namespace = "someNamespace";
		final var emailAddress = Collections.singletonList("someEmailAddress");
		final var metadata = Collections.singletonMap("someKey", "someValue");

		// Act
		final var bean = GraphCredentialsEntity.builder()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withDestinationFolder(destinationFolder)
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withNamespace(namespace)
			.withEmailAddress(emailAddress)
			.withMetadata(metadata)
			.build();

		bean.prePersist();

		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getDestinationFolder()).isEqualTo(destinationFolder);
		assertThat(bean.getClientId()).isEqualTo(clientId);
		assertThat(bean.getClientSecret()).isEqualTo(clientSecret);
		assertThat(bean.getTenantId()).isEqualTo(tenantId);
		assertThat(bean.getNamespace()).isEqualTo(namespace);
		assertThat(bean.getMetadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(bean.getEmailAddress()).hasSize(1).element(0)
			.satisfies(address -> assertThat(address).isEqualTo("someEmailAddress"));
		assertThat(bean.getCreatedAt()).isCloseTo(now(), within(1, SECONDS));
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(GraphCredentialsEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new GraphCredentialsEntity()).hasAllNullFieldsOrProperties();
	}
}
