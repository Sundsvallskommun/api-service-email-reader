package se.sundsvall.emailreader.service;

import static java.util.Collections.emptyList;

import java.util.List;
import org.springframework.stereotype.Service;
import se.sundsvall.emailreader.api.model.GraphCredentials;

@Service
public class GraphCredentialsService {

	public List<GraphCredentials> getCredentialsByMunicipalityId(final String municipalityId) {
		return emptyList();
	}

	public String create(final String municipalityId, final GraphCredentials credentials) {
		return null;
	}

	public void update(final String municipalityId, final String id, final GraphCredentials credentials) {}

	public void delete(final String municipalityId, final String id) {}
}
