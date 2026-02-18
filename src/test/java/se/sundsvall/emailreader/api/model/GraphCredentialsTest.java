package se.sundsvall.emailreader.api.model;

import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GraphCredentialsTest {

	@Test
	void builder() {
		// Arrange
		final var destinationFolder = "someDestinationFolder";
		final var clientId = "someClientId";
		final var clientSecret = "someClientSecret";
		final var tenantId = "someTenantId";
		final var namespace = "someNamespace";
		final var emailAddress = Collections.singletonList("someEmailAddress");
		final var metadata = Collections.singletonMap("someKey", "someValue");
		final var id = "someId";
		final var enabled = true;

		// Act
		final var bean = GraphCredentials.builder()
			.withDestinationFolder(destinationFolder)
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withNamespace(namespace)
			.withEmailAddress(emailAddress)
			.withMetadata(metadata)
			.withId(id)
			.withEnabled(enabled)
			.build();
		// Assert
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.id()).isEqualTo(id);
		assertThat(bean.destinationFolder()).isEqualTo(destinationFolder);
		assertThat(bean.clientId()).isEqualTo(clientId);
		assertThat(bean.clientSecret()).isEqualTo(clientSecret);
		assertThat(bean.tenantId()).isEqualTo(tenantId);
		assertThat(bean.namespace()).isEqualTo(namespace);
		assertThat(bean.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(bean.emailAddress()).hasSize(1).element(0)
			.satisfies(address -> assertThat(address).isEqualTo("someEmailAddress"));
		assertThat(bean.enabled()).isEqualTo(enabled);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(GraphCredentials.builder().build()).hasAllNullFieldsOrPropertiesExcept("enabled").satisfies(
			bean -> assertThat(bean.enabled()).isFalse());
	}

}
