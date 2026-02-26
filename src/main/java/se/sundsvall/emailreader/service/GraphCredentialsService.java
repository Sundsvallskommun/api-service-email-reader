package se.sundsvall.emailreader.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.emailreader.api.model.GraphCredentials;
import se.sundsvall.emailreader.integration.db.GraphCredentialsRepository;
import se.sundsvall.emailreader.service.mapper.GraphCredentialsMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.emailreader.service.mapper.GraphCredentialsMapper.toGraphCredentials;
import static se.sundsvall.emailreader.service.mapper.GraphCredentialsMapper.toGraphCredentialsEntity;

@Service
public class GraphCredentialsService {

	private static final String ENTITY_NOT_FOUND = "Entity not found";
	private final GraphCredentialsRepository graphCredentialsRepository;

	private final EncryptionUtility encryptionUtility;

	public GraphCredentialsService(final GraphCredentialsRepository graphCredentialsRepository, final EncryptionUtility encryptionUtility) {
		this.graphCredentialsRepository = graphCredentialsRepository;
		this.encryptionUtility = encryptionUtility;
	}

	public List<GraphCredentials> getCredentialsByMunicipalityId(final String municipalityId) {

		return toGraphCredentials(graphCredentialsRepository.findAllByMunicipalityId(municipalityId));
	}

	public GraphCredentials getCredentialsByMunicipalityIdAndId(final String municipalityId, final String id) {

		return graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)
			.map(GraphCredentialsMapper::toGraphCredential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ENTITY_NOT_FOUND));
	}

	@Transactional
	public String create(final String municipalityId, final GraphCredentials credentials) {

		final var entity = toGraphCredentialsEntity(encrypt(credentials), municipalityId);

		return graphCredentialsRepository.save(entity).getId();
	}

	@Transactional
	public void update(final String municipalityId, final String id, final GraphCredentials credentials) {

		final var oldEntity = graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ENTITY_NOT_FOUND));

		final var updatedEntity = GraphCredentialsMapper.toUpdatedGraphCredentialsEntity(oldEntity, encrypt(credentials));
		graphCredentialsRepository.save(updatedEntity);

	}

	@Transactional
	public void delete(final String municipalityId, final String id) {
		final var oldEntity = graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ENTITY_NOT_FOUND));
		graphCredentialsRepository.delete(oldEntity);
	}

	private GraphCredentials encrypt(final GraphCredentials credentials) {
		return credentials
			.withClientId(encryptionUtility.encrypt(credentials.clientId().getBytes()))
			.withClientSecret(encryptionUtility.encrypt(credentials.clientSecret().getBytes()))
			.withTenantId(encryptionUtility.encrypt(credentials.tenantId().getBytes()));
	}

}
