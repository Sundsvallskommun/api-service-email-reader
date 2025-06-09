package se.sundsvall.emailreader.service.mapper;

import java.util.List;
import java.util.Optional;
import se.sundsvall.emailreader.api.model.GraphCredentials;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;

public final class GraphCredentialsMapper {

	private GraphCredentialsMapper() {
		// Prevent instantiation
	}

	public static List<GraphCredentials> toGraphCredentials(final List<GraphCredentialsEntity> graphCredentialsEntities) {
		return graphCredentialsEntities.stream()
			.map(GraphCredentialsMapper::toGraphCredential)
			.toList();
	}

	public static GraphCredentials toGraphCredential(final GraphCredentialsEntity graphCredentialsEntity) {
		return GraphCredentials.builder()
			.withDestinationFolder(graphCredentialsEntity.getDestinationFolder())
			.withNamespace(graphCredentialsEntity.getNamespace())
			.withEmailAddress(graphCredentialsEntity.getEmailAddress())
			.withMetadata(graphCredentialsEntity.getMetadata())
			.withId(graphCredentialsEntity.getId())
			.withEnabled(graphCredentialsEntity.isEnabled())
			.build();
	}

	public static GraphCredentialsEntity toGraphCredentialsEntity(final GraphCredentials graphCredentials, final String municipalityId) {
		return GraphCredentialsEntity.builder()
			.withId(graphCredentials.id())
			.withMunicipalityId(municipalityId)
			.withDestinationFolder(graphCredentials.destinationFolder())
			.withClientId(graphCredentials.clientId())
			.withClientSecret(graphCredentials.clientSecret())
			.withTenantId(graphCredentials.tenantId())
			.withNamespace(graphCredentials.namespace())
			.withEmailAddress(graphCredentials.emailAddress())
			.withMetadata(graphCredentials.metadata())
			.withEnabled(graphCredentials.enabled())
			.build();
	}

	public static GraphCredentialsEntity toUpdatedGraphCredentialsEntity(final GraphCredentialsEntity entity, final GraphCredentials credentials) {

		Optional.ofNullable(credentials.destinationFolder()).ifPresent(entity::setDestinationFolder);
		Optional.ofNullable(credentials.clientId()).ifPresent(entity::setClientId);
		Optional.ofNullable(credentials.clientSecret()).ifPresent(entity::setClientSecret);
		Optional.ofNullable(credentials.tenantId()).ifPresent(entity::setTenantId);
		Optional.ofNullable(credentials.namespace()).ifPresent(entity::setNamespace);
		Optional.ofNullable(credentials.emailAddress()).ifPresent(entity::setEmailAddress);
		Optional.ofNullable(credentials.metadata()).ifPresent(entity::setMetadata);
		Optional.of(credentials.enabled()).ifPresent(entity::setEnabled);
		return entity;
	}
}
