package se.sundsvall.emailreader.service.mapper;

import java.util.List;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

public class CredentialsMapper {

	public CredentialsEntity toEntity(final String municipalityId, final Credentials credentials, final String password) {

		return CredentialsEntity.builder()
			.withDomain(credentials.domain())
			.withMunicipalityId(municipalityId)
			.withNamespace(credentials.namespace())
			.withUsername(credentials.username())
			.withEmailAddress(credentials.emailAddress())
			.withDestinationFolder(credentials.destinationFolder())
			.withPassword(password)
			.withMetadata(credentials.metadata())
			.build();
	}

	public Credentials toDto(final CredentialsEntity entity) {
		return Credentials.builder()
			.withId(entity.getId())
			.withDomain(entity.getDomain())
			.withEmailAddress(entity.getEmailAddress())
			.withNamespace(entity.getNamespace())
			.withUsername(entity.getUsername())
			.withMetadata(entity.getMetadata())
			.withDestinationFolder(entity.getDestinationFolder())
			.build();
	}

	public List<Credentials> toDtos(final List<CredentialsEntity> entities) {

		return entities.stream()
			.map(this::toDto)
			.toList();
	}

	public void updateEntity(final CredentialsEntity entity, final Credentials credentials, final String password) {

		entity.setDomain(credentials.domain());
		entity.setNamespace(credentials.namespace());
		entity.setUsername(credentials.username());
		entity.setDestinationFolder(credentials.destinationFolder());
		entity.setPassword(password);
		entity.setEmailAddress(credentials.emailAddress());
		entity.setMetadata(credentials.metadata());

	}

}
