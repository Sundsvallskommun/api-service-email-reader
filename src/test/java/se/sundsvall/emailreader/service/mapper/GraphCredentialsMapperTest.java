package se.sundsvall.emailreader.service.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.emailreader.api.model.GraphCredentials;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;

import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class GraphCredentialsMapperTest {

	@Test
	void toGraphCredentials() {
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

		final var entity = GraphCredentialsEntity.builder()
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

		// Act
		final var result = GraphCredentialsMapper.toGraphCredentials(List.of(entity));

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasNoNullFieldsOrPropertiesExcept("clientSecret", "tenantId", "clientId");
		assertThat(result.getFirst().id()).isEqualTo(id);
		assertThat(result.getFirst().destinationFolder()).isEqualTo(destinationFolder);
		assertThat(result.getFirst().namespace()).isEqualTo(namespace);
		assertThat(result.getFirst().metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.getFirst().emailAddress()).hasSize(1).element(0)
			.satisfies(address -> assertThat(address).isEqualTo("someEmailAddress"));
	}

	@Test
	void toGraphCredential() {
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
		final var entity = GraphCredentialsEntity.builder()
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
		// Act
		final var result = GraphCredentialsMapper.toGraphCredential(entity);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("clientSecret", "tenantId", "clientId");
		assertThat(result.id()).isEqualTo(id);
		assertThat(result.destinationFolder()).isEqualTo(destinationFolder);
		assertThat(result.namespace()).isEqualTo(namespace);
		assertThat(result.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.emailAddress()).hasSize(1).element(0)
			.satisfies(address -> assertThat(address).isEqualTo("someEmailAddress"));
	}

	@Test
	void toGraphCredentialsEntity() {
		// Arrange
		final var municipalityId = "someMunicipalityId";
		final var id = "someId";
		final var destinationFolder = "someDestinationFolder";
		final var clientId = "someClientId";
		final var clientSecret = "someClientSecret";
		final var tenantId = "someTenantId";
		final var namespace = "someNamespace";
		final var emailAddress = Collections.singletonList("someEmailAddress");
		final var metadata = Collections.singletonMap("someKey", "someValue");

		final var credentials = GraphCredentials.builder()
			.withDestinationFolder(destinationFolder)
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withNamespace(namespace)
			.withEmailAddress(emailAddress)
			.withMetadata(metadata)
			.withId(id)
			.build();

		// Act
		final var result = GraphCredentialsMapper.toGraphCredentialsEntity(credentials, municipalityId);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("createdAt");
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(result.getDestinationFolder()).isEqualTo(destinationFolder);
		assertThat(result.getClientId()).isEqualTo(clientId);
		assertThat(result.getClientSecret()).isEqualTo(clientSecret);
		assertThat(result.getTenantId()).isEqualTo(tenantId);
		assertThat(result.getNamespace()).isEqualTo(namespace);
		assertThat(result.getMetadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(result.getEmailAddress()).hasSize(1).element(0)
			.satisfies(address -> assertThat(address).isEqualTo("someEmailAddress"));
		assertThat(result.getCreatedAt()).isNull();
	}

	@Test
	void toUpdatedGraphCredentialsEntity() {
		// Arrange
		final var municipalityId = "someMunicipalityId";
		final var id = "74531aac-ffea-42cc-8a0a-52715bb27850";
		final var oldClientSecret = "oldSecret";
		final var oldTenantId = "oldTenantId";
		final var oldClientId = "oldClientId";
		final var oldEmailAddress = List.of("oldemail@sundsvall.se");
		final var oldNamespace = "old.namespace";
		final var oldDestinationFolder = "OldProcessed";
		final var oldMetadata = Map.of("oldkey", "oldvalue");
		final var createdAt = now();

		final var newClientSecret = "newSecret";
		final var newTenantId = "newTenantId";
		final var newClientId = "newClientId";
		final var newEmailAddress = List.of("newemail@sundsvall.se");
		final var newNamespace = "new.namespace";
		final var newDestinationFolder = "NewProcessed";
		final var newMetadata = Map.of("newkey", "newvalue");

		final var entity = GraphCredentialsEntity.builder()
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withClientSecret(oldClientSecret)
			.withTenantId(oldTenantId)
			.withClientId(oldClientId)
			.withEmailAddress(oldEmailAddress)
			.withNamespace(oldNamespace)
			.withDestinationFolder(oldDestinationFolder)
			.withMetadata(oldMetadata)
			.withCreatedAt(createdAt)
			.build();

		final var credentials = GraphCredentials.builder()
			.withClientSecret(newClientSecret)
			.withTenantId(newTenantId)
			.withClientId(newClientId)
			.withEmailAddress(newEmailAddress)
			.withNamespace(newNamespace)
			.withDestinationFolder(newDestinationFolder)
			.withMetadata(newMetadata)
			.build();

		// Act
		final var result = GraphCredentialsMapper.toUpdatedGraphCredentialsEntity(entity, credentials);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getClientSecret()).isEqualTo(newClientSecret);
		assertThat(result.getTenantId()).isEqualTo(newTenantId);
		assertThat(result.getClientId()).isEqualTo(newClientId);
		assertThat(result.getEmailAddress()).containsExactly(newEmailAddress.getFirst());
		assertThat(result.getNamespace()).isEqualTo(newNamespace);
		assertThat(result.getDestinationFolder()).isEqualTo(newDestinationFolder);
		assertThat(result.getMetadata()).isEqualTo(newMetadata);
		assertThat(result.getCreatedAt()).isEqualTo(createdAt);
	}
}
