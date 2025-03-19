package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.azure.identity.ClientSecretCredential;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.MessageCollectionResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
class GraphIntegrationTest {

	@Mock
	private GraphClient graphClient;

	@Mock
	private GraphMapper graphMapper;

	@Mock
	private EncryptionUtility encryptionUtility;

	@InjectMocks
	private GraphIntegration graphIntegration;

	@Mock
	private Consumer<String> consumerMock;

	@Test
	void faultyCredentials() {
		// Arrange
		final var userId = "userId";
		final var credentials = GraphCredentialsEntity.builder().build();

		// Act
		assertThatThrownBy(() -> graphIntegration.getEmails(userId, credentials, consumerMock))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Must provide non-null values for clientId, tenantId, clientSecret properties in ClientSecretCredentialBuilder");
	}

	@Test
	void testGetEmails() {
		// Arrange
		final var userId = "userId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.build();

		final var messageCollectionResponse = mock(MessageCollectionResponse.class);
		final var messages = List.of(new Message());
		when(messageCollectionResponse.getValue()).thenReturn(messages);
		when(graphClient.getInbox(userId, consumerMock)).thenReturn(messageCollectionResponse);
		when(encryptionUtility.decrypt(clientId)).thenReturn("decryptedClientId");
		when(encryptionUtility.decrypt(clientSecret)).thenReturn("decryptedClientSecret");
		when(encryptionUtility.decrypt(tenantId)).thenReturn("decryptedTenantId");

		final var emailEntities = List.of(new EmailEntity());
		when(graphMapper.toEmails(messages, municipalityId, namespace, metadata)).thenReturn(emailEntities);

		// Act
		final var result = graphIntegration.getEmails(userId, credentials, consumerMock);

		// Assert
		assertThat(result).isNotNull().hasSize(1).containsExactlyElementsOf(emailEntities);
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), eq(consumerMock));
		verify(graphClient).getInbox(userId, consumerMock);
		verify(graphMapper).toEmails(messages, municipalityId, namespace, metadata);
		verify(encryptionUtility).decrypt(clientId);
		verify(encryptionUtility).decrypt(clientSecret);
		verify(encryptionUtility).decrypt(tenantId);
		verifyNoMoreInteractions(graphClient, graphMapper, encryptionUtility, consumerMock);
	}

	@Test
	void testGetEmailsNoMessagesFound() {
		// Arrange
		final var userId = "userId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.build();

		final var messageCollectionResponse = mock(MessageCollectionResponse.class);
		when(messageCollectionResponse.getValue()).thenReturn(null);
		when(graphClient.getInbox(userId, consumerMock)).thenReturn(messageCollectionResponse);
		when(encryptionUtility.decrypt(clientId)).thenReturn("decryptedClientId");
		when(encryptionUtility.decrypt(clientSecret)).thenReturn("decryptedClientSecret");
		when(encryptionUtility.decrypt(tenantId)).thenReturn("decryptedTenantId");

		// Act
		final var result = graphIntegration.getEmails(userId, credentials, consumerMock);

		// Assert
		assertThat(result).isEmpty();
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), eq(consumerMock));
		verify(graphClient).getInbox(userId, consumerMock);
		verify(graphMapper).toEmails(emptyList(), municipalityId, namespace, metadata);
		verify(encryptionUtility).decrypt(clientId);
		verify(encryptionUtility).decrypt(clientSecret);
		verify(encryptionUtility).decrypt(tenantId);
		verifyNoMoreInteractions(graphClient, graphMapper, encryptionUtility, consumerMock);

	}

	@Test
	void testGetAttachments() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.build();
		final var attachments = List.of(new Attachment());
		final var attachmentEntities = List.of(new AttachmentEntity());
		when(graphClient.getAttachments(userId, messageId, consumerMock)).thenReturn(attachments);
		when(graphMapper.toAttachments(attachments)).thenReturn(attachmentEntities);
		when(encryptionUtility.decrypt(clientId)).thenReturn("decryptedClientId");
		when(encryptionUtility.decrypt(clientSecret)).thenReturn("decryptedClientSecret");
		when(encryptionUtility.decrypt(tenantId)).thenReturn("decryptedTenantId");

		// Act
		final var result = graphIntegration.getAttachments(userId, credentials, messageId, consumerMock);

		// Assert
		assertThat(result).isNotNull().hasSize(1).containsExactlyElementsOf(attachmentEntities);
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), eq(consumerMock));
		verify(graphClient).getAttachments(userId, messageId, consumerMock);
		verify(graphMapper).toAttachments(attachments);
		verify(encryptionUtility).decrypt(clientId);
		verify(encryptionUtility).decrypt(clientSecret);
		verify(encryptionUtility).decrypt(tenantId);
		verifyNoMoreInteractions(graphClient, graphMapper, encryptionUtility, consumerMock);
	}

	@Test
	void testMoveEmail() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final var destinationFolder = "Archive";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.withDestinationFolder(destinationFolder)
			.build();

		when(encryptionUtility.decrypt(clientId)).thenReturn("decryptedClientId");
		when(encryptionUtility.decrypt(clientSecret)).thenReturn("decryptedClientSecret");
		when(encryptionUtility.decrypt(tenantId)).thenReturn("decryptedTenantId");

		// Act
		graphIntegration.moveEmail(userId, messageId, credentials, consumerMock);

		// Assert
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), eq(consumerMock));
		verify(graphClient).moveEmail(userId, messageId, destinationFolder, consumerMock);
		verify(encryptionUtility).decrypt(clientId);
		verify(encryptionUtility).decrypt(clientSecret);
		verify(encryptionUtility).decrypt(tenantId);
		verifyNoMoreInteractions(graphClient, graphMapper, encryptionUtility, consumerMock);
	}
}
