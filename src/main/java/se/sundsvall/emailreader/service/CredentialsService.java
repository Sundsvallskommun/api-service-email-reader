package se.sundsvall.emailreader.service;


import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.service.mapper.CredentialsMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@Service
public class CredentialsService {

	private final EncryptionUtility encryptionUtility;

	private final CredentialsRepository credentialsRepository;

	private final CredentialsMapper credentialsMapper = new CredentialsMapper();

	public CredentialsService(final EncryptionUtility encryptionUtility, final CredentialsRepository credentialsRepository) {
		this.encryptionUtility = encryptionUtility;
		this.credentialsRepository = credentialsRepository;
	}

	public List<Credentials> getCredentialsByMunicipalityId(final String municipalityId) {

		return credentialsMapper.toDtos(credentialsRepository.findAllByMunicipalityId(municipalityId));
	}

	public void create(final String municipalityId, final Credentials credentials) {
		final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());

		credentialsRepository.save(credentialsMapper.toEntity(municipalityId, credentials, encryptedPassword));
	}

	@Transactional
	public void delete(final String municipalityId, final String id) {

		credentialsRepository.deleteByMunicipalityIdAndId(municipalityId, id);
	}

	public void update(final String municipalityId, final String id, final Credentials credentials) {

		final var entity = credentialsRepository.findByMunicipalityIdAndId(municipalityId, id).orElseThrow();

		final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());

		credentialsMapper.updateEntity(entity, credentials, encryptedPassword);

		credentialsRepository.save(entity);

	}

}
