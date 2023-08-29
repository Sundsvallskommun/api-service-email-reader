package se.sundsvall.emailreader.service;


import java.util.List;

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

    public List<Credentials> getAllCredentials() {

        return credentialsMapper.toDtos(credentialsRepository.findAll());
    }

    public void create(Credentials credentials) {

        final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());
        credentials = credentials.withPassword(encryptedPassword);

        credentialsRepository.save(credentialsMapper.toEntity(credentials));
    }

    public void delete(final String id) {

        credentialsRepository.deleteById(id);
    }

    public void update(final String id, Credentials credentials) {

        final var entity = credentialsRepository.findById(id).orElseThrow();

        final var encryptedPassword = encryptionUtility.encrypt(credentials.password().getBytes());
        credentials = credentials.withPassword(encryptedPassword);

        credentialsMapper.updateEntity(entity, credentials);

        credentialsRepository.save(entity);

    }

}
