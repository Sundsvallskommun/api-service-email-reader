package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

@Component
public class GraphIntegration {
	private final GraphClient graphClient;
	private final GraphMapper graphMapper;

	public GraphIntegration(final GraphClient graphClient, final GraphMapper graphMapper) {
		this.graphClient = graphClient;
		this.graphMapper = graphMapper;
	}

	public List<EmailEntity> getEmails(final String userId, final String folderId, final String municipalityId, final String namespace, final Map<String, String> metadata) {

		final var graphScopes = new String[] {
			"https://graph.microsoft.com/.default"
		};

		final var clientSecretCredential = getClientSecretCredential();

		graphClient.initializeGraph(clientSecretCredential, graphScopes);
		final var messages = Optional.ofNullable(graphClient.getInbox(userId, folderId).getValue())
			.orElse(emptyList());

		return graphMapper.toEmails(messages, municipalityId, namespace, metadata);
	}

	private ClientSecretCredential getClientSecretCredential() {
		return new ClientSecretCredentialBuilder()
			.clientId("TBD") // Will be fetched from db at a later stage and not hardcoded dummy values
			.clientSecret("TBD")
			.tenantId("TBD")
			.build();
	}
}
