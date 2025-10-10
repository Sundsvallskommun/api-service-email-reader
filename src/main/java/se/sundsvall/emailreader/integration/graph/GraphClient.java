package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.MailFolder;
import com.microsoft.graph.models.MessageCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.messages.item.move.MovePostRequestBody;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@CircuitBreaker(name = "GraphIntegration")
public class GraphClient {
	private static final String[] GRAPH_SCOPES = new String[] {
		"https://graph.microsoft.com/.default"
	};

	private static final Logger LOG = LoggerFactory.getLogger(GraphClient.class);
	private GraphServiceClient graphServiceClient;

	/**
	 * Initialize the graph client with the client secret credential and the graph scopes to use
	 *
	 * @param clientSecretCredential the client secret credential
	 */
	void initializeGraph(final TokenCredential clientSecretCredential, final Consumer<String> setUnHealthyConsumer) {
		try {
			graphServiceClient = new GraphServiceClient(clientSecretCredential, GRAPH_SCOPES);

		} catch (final Exception e) {
			LOG.error("Could not initialize graph client", e);
			setUnHealthyConsumer.accept("[GRAPH] Could not initialize graph client");
		}
	}

	/**
	 * Get all messages in the inbox folder for a user with a specific folder id
	 *
	 * @param  userId the user id to get the inbox for
	 * @return        a collection of messages
	 */
	MessageCollectionResponse getInbox(final String userId, final Consumer<String> setUnHealthyConsumer) {
		try {
			return graphServiceClient
				.users()
				.byUserId(userId)
				.mailFolders()
				.byMailFolderId("inbox")
				.messages()
				.get(requestConfiguration -> requestConfiguration.queryParameters.select = new String[] {
					"id", "toRecipients", "sender", "subject", "internetMessageHeaders", "body", "receivedDateTime"
				});

		} catch (final Exception e) {
			LOG.error("Could not get inbox", e);
			setUnHealthyConsumer.accept("[GRAPH] Could not get inbox");
			return null;
		}
	}

	List<Attachment> getAttachments(final String userId, final String messageId, final Consumer<String> setUnHealthyConsumer) {
		try {
			final var response = graphServiceClient
				.users()
				.byUserId(userId)
				.messages()
				.byMessageId(messageId)
				.attachments()
				.get();

			if (response == null) {
				return emptyList();
			}

			return response.getValue();
		} catch (final Exception e) {
			LOG.error("Could not get attachment", e);
			setUnHealthyConsumer.accept("[GRAPH] not get attachments for email");
			return emptyList();
		}
	}

	public void moveEmail(final String userId, final String messageId, final String destinationFolder, final Consumer<String> setUnHealthyConsumer) {
		try {
			final var request = new MovePostRequestBody();
			request.setDestinationId(findFolderId(userId, destinationFolder));

			graphServiceClient
				.users()
				.byUserId(userId)
				.messages()
				.byMessageId(messageId)
				.move()
				.post(request);
		} catch (final Exception e) {
			LOG.error("Could not move email", e);
			setUnHealthyConsumer.accept("[GRAPH] Could not move email from inbox");
		}
	}

	private String findFolderId(final String userId, final String folderId) {
		try {
			final var result = graphServiceClient
				.users()
				.byUserId(userId)
				.mailFolders()
				.get();

			if (result == null) {
				return createFolder(userId, folderId);
			}

			final Optional<MailFolder> optionalFolder = Objects.requireNonNull(result.getValue())
				.stream()
				.filter(mailFolder -> Objects.requireNonNull(mailFolder.getDisplayName()).equalsIgnoreCase(folderId))
				.findFirst();

			if (optionalFolder.isPresent()) {
				return optionalFolder.get().getId();
			} else {
				return createFolder(userId, folderId);
			}

		} catch (final Exception e) {
			LOG.error("Could not find folder");
			throw e;
		}
	}

	private String createFolder(final String userId, final String folderId) {
		final MailFolder mailFolder = new MailFolder();
		mailFolder.setDisplayName(folderId);
		try {
			final var result = graphServiceClient
				.users()
				.byUserId(userId)
				.mailFolders()
				.post(mailFolder);
			return Objects.requireNonNull(result).getId();
		} catch (final Exception e) {
			LOG.error("Could not create folder");
			throw e;
		}
	}
}
