package se.sundsvall.emailreader.integration.graph;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.identity.ClientSecretCredential;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.MessageCollectionResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

@ExtendWith(MockitoExtension.class)
class GraphIntegrationTest {

	@Mock
	private GraphClient graphClient;

	@Mock
	private GraphMapper graphMapper;

	@InjectMocks
	private GraphIntegration graphIntegration;

	@Test
	void testGetEmails() {
		// Arrange
		final var userId = "userId";
		final var folderId = "folderId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final Map<String, String> metadata = emptyMap();

		final var messageCollectionResponse = mock(MessageCollectionResponse.class);
		final var messages = List.of(new Message());
		when(messageCollectionResponse.getValue()).thenReturn(messages);
		when(graphClient.getInbox(userId, folderId)).thenReturn(messageCollectionResponse);

		final var emailEntities = List.of(new EmailEntity());
		when(graphMapper.toEmails(messages, municipalityId, namespace, metadata)).thenReturn(emailEntities);

		// Act
		final var result = graphIntegration.getEmails(userId, folderId, municipalityId, namespace, metadata);

		// Assert
		assertThat(result).isNotNull().hasSize(1).containsExactlyElementsOf(emailEntities);
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), any(String[].class));
		verify(graphClient).getInbox(userId, folderId);
		verify(graphMapper).toEmails(messages, municipalityId, namespace, metadata);
	}

	@Test
	void testGetEmailsNoMessagesFound() {
		// Arrange
		final var userId = "userId";
		final var folderId = "folderId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final Map<String, String> metadata = emptyMap();

		final var messageCollectionResponse = mock(MessageCollectionResponse.class);
		when(messageCollectionResponse.getValue()).thenReturn(null);
		when(graphClient.getInbox(userId, folderId)).thenReturn(messageCollectionResponse);

		// Act
		final var result = graphIntegration.getEmails(userId, folderId, municipalityId, namespace, metadata);

		// Assert
		assertThat(result).isEmpty();
		verify(graphClient).initializeGraph(any(ClientSecretCredential.class), any(String[].class));
		verify(graphClient).getInbox(userId, folderId);
		verify(graphMapper).toEmails(emptyList(), municipalityId, namespace, metadata);
	}
}
