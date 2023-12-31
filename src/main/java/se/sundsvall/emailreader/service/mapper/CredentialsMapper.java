package se.sundsvall.emailreader.service.mapper;

import java.util.List;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

public class CredentialsMapper {

	public CredentialsEntity toEntity(final Credentials credentials) {

		return CredentialsEntity.builder()
			.withDomain(credentials.domain())
			.withMunicipalityId(credentials.municipalityId())
			.withNamespace(credentials.namespace())
			.withUsername(credentials.username())
			.withEmailAddress(credentials.emailAddress())
			.withDestinationFolder(credentials.destinationFolder())
			.withPassword(credentials.password())
			.withMetadata(credentials.metadata())
			.build();
	}

	public Credentials toDto(final CredentialsEntity entity) {
		return Credentials.builder()
			.withId(entity.getId())
			.withDomain(entity.getDomain())
			.withEmailAddress(entity.getEmailAddress())
			.withMunicipalityId(entity.getMunicipalityId())
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

	public void updateEntity(final CredentialsEntity entity, final Credentials credentials) {

		entity.setDomain(credentials.domain());
		entity.setNamespace(credentials.namespace());
		entity.setMunicipalityId(credentials.municipalityId());
		entity.setUsername(credentials.username());
		entity.setDestinationFolder(credentials.destinationFolder());
		entity.setPassword(credentials.password());
		entity.setEmailAddress(credentials.emailAddress());
		entity.setMetadata(credentials.metadata());

	}

}
