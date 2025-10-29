package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.MessageCollectionResponse;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@Component
public class GraphIntegration {
	private final GraphClient graphClient;
	private final GraphMapper graphMapper;
	private final EncryptionUtility encryptionUtility;

	public GraphIntegration(final GraphClient graphClient, final GraphMapper graphMapper, final EncryptionUtility encryptionUtility) {
		this.graphClient = graphClient;
		this.graphMapper = graphMapper;
		this.encryptionUtility = encryptionUtility;
	}

	public List<EmailEntity> getEmails(final String userId, final GraphCredentialsEntity credentials, final Consumer<String> setUnHealthyConsumer) {
		final var clientSecretCredential = getClientSecretCredential(credentials);
		graphClient.initializeGraph(clientSecretCredential, setUnHealthyConsumer);

		final var messages = Optional.ofNullable(graphClient.getInbox(userId, setUnHealthyConsumer))
			.map(MessageCollectionResponse::getValue)
			.orElse(emptyList());

		return graphMapper.toEmails(messages, credentials.getMunicipalityId(), credentials.getNamespace(), credentials.getMetadata());
	}

	public List<AttachmentEntity> getAttachments(final String userId, final GraphCredentialsEntity credentials, final String messageId, final Consumer<String> setUnHealthyConsumer) {
		final var clientSecretCredential = getClientSecretCredential(credentials);
		graphClient.initializeGraph(clientSecretCredential, setUnHealthyConsumer);

		final var attachments = graphClient.getAttachments(userId, messageId, setUnHealthyConsumer);
		return graphMapper.toAttachments(attachments);
	}

	public void moveEmail(final String userId, final String messageId, final GraphCredentialsEntity credentials, final Consumer<String> setUnHealthyConsumer) {

		final var clientSecretCredential = getClientSecretCredential(credentials);
		graphClient.initializeGraph(clientSecretCredential, setUnHealthyConsumer);

		graphClient.moveEmail(userId, messageId, credentials.getDestinationFolder(), setUnHealthyConsumer);
	}

	private ClientSecretCredential getClientSecretCredential(final GraphCredentialsEntity credentials) {
		return new ClientSecretCredentialBuilder()
			.clientId(encryptionUtility.decrypt(credentials.getClientId()))
			.clientSecret(encryptionUtility.decrypt(credentials.getClientSecret()))
			.tenantId(encryptionUtility.decrypt(credentials.getTenantId()))
			.build();
	}
}
