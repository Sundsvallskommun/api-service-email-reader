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
            .withEmailAdress(credentials.emailAdress())
            .withDestinationFolder(credentials.destinationFolder())
            .withPassword(credentials.password())
            .build();
    }

    public Credentials toDto(final CredentialsEntity entity) {
        return Credentials.builder()
            .withId(entity.getId())
            .withDomain(entity.getDomain())
            .withEmailAdress(entity.getEmailAdress())
            .withMunicipalityId(entity.getMunicipalityId())
            .withNamespace(entity.getNamespace())
            .withUsername(entity.getUsername())
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
        entity.setEmailAdress(credentials.emailAdress());

    }

}
