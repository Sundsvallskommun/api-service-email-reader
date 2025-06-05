package se.sundsvall.emailreader.service;

import static se.sundsvall.emailreader.service.mapper.CredentialsMapper.toDtos;
import static se.sundsvall.emailreader.service.mapper.CredentialsMapper.toEntity;
import static se.sundsvall.emailreader.service.mapper.CredentialsMapper.updateEntity;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@Service
public class CredentialsService {

	private final EncryptionUtility encryptionUtility;

	private final CredentialsRepository credentialsRepository;

	public CredentialsService(final EncryptionUtility encryptionUtility, final CredentialsRepository credentialsRepository) {
		this.encryptionUtility = encryptionUtility;
		this.credentialsRepository = credentialsRepository;
	}

	public List<Credentials> getCredentialsByMunicipalityId(final String municipalityId) {
		return toDtos(credentialsRepository.findAllByMunicipalityId(municipalityId));
	}

	public void create(final String municipalityId, final Credentials credentials) {
		final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());

		credentialsRepository.save(toEntity(municipalityId, credentials, encryptedPassword));
	}

	@Transactional
	public void delete(final String municipalityId, final String id) {
		credentialsRepository.deleteByMunicipalityIdAndId(municipalityId, id);
	}

	public void update(final String municipalityId, final String id, final Credentials credentials) {

		final var entity = credentialsRepository.findByMunicipalityIdAndId(municipalityId, id).orElseThrow();

		final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());

		updateEntity(entity, credentials, encryptedPassword);

		credentialsRepository.save(entity);
	}
}
