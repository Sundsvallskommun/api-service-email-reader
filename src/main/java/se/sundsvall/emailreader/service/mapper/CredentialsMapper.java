package se.sundsvall.emailreader.service.mapper;

import java.util.List;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

public final class CredentialsMapper {

	private CredentialsMapper() {
		// Intentionally empty
	}

	public static CredentialsEntity toEntity(final String municipalityId, final Credentials credentials, final String password) {
		return CredentialsEntity.builder()
			.withDomain(credentials.domain())
			.withMunicipalityId(municipalityId)
			.withNamespace(credentials.namespace())
			.withUsername(credentials.username())
			.withEmailAddress(credentials.emailAddress())
			.withDestinationFolder(credentials.destinationFolder())
			.withPassword(password)
			.withMetadata(credentials.metadata())
			.withAction(credentials.action())
			.build();
	}

	public static Credentials toDto(final CredentialsEntity entity) {
		return Credentials.builder()
			.withId(entity.getId())
			.withDomain(entity.getDomain())
			.withEmailAddress(entity.getEmailAddress())
			.withNamespace(entity.getNamespace())
			.withUsername(entity.getUsername())
			.withMetadata(entity.getMetadata())
			.withDestinationFolder(entity.getDestinationFolder())
			.withAction(entity.getAction())
			.build();
	}

	public static List<Credentials> toDtos(final List<CredentialsEntity> entities) {
		return entities.stream()
			.map(CredentialsMapper::toDto)
			.toList();
	}

	public static void updateEntity(final CredentialsEntity entity, final Credentials credentials, final String password) {
		entity.setDomain(credentials.domain());
		entity.setNamespace(credentials.namespace());
		entity.setUsername(credentials.username());
		entity.setDestinationFolder(credentials.destinationFolder());
		entity.setPassword(password);
		entity.setEmailAddress(credentials.emailAddress());
		entity.setMetadata(credentials.metadata());
		entity.setAction(credentials.action());
	}
}
