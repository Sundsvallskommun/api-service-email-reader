package se.sundsvall.emailreader.integration.graph;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.MessageCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
@CircuitBreaker(name = "GraphIntegration")
public class GraphClient {

	private GraphServiceClient graphServiceClient;

	/**
	 * Initialize the graph client with the client secret credential and the graph scopes to use
	 *
	 * @param clientSecretCredential the client secret credential
	 * @param graphScopes            the graph scopes
	 */
	void initializeGraph(final TokenCredential clientSecretCredential, final String[] graphScopes) {
		graphServiceClient = new GraphServiceClient(clientSecretCredential, graphScopes);
	}

	/**
	 * Get all messages in the inbox folder for a user with a specific folder id
	 *
	 * @param  userId   the user id to get the inbox for
	 * @param  folderId the folder id to get the messages from
	 * @return          a collection of messages
	 */
	MessageCollectionResponse getInbox(final String userId, final String folderId) {

		return graphServiceClient
			.users()
			.byUserId(userId)
			.mailFolders()
			.byMailFolderId(folderId)
			.messages()
			.get();
	}
}
