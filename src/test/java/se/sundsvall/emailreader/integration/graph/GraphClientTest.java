package se.sundsvall.emailreader.integration.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.MessageCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.UsersRequestBuilder;
import com.microsoft.graph.users.item.UserItemRequestBuilder;
import com.microsoft.graph.users.item.mailfolders.MailFoldersRequestBuilder;
import com.microsoft.graph.users.item.mailfolders.item.MailFolderItemRequestBuilder;
import com.microsoft.graph.users.item.mailfolders.item.messages.MessagesRequestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphClientTest {

	@Mock
	MailFolderItemRequestBuilder mailFolderItemRequestBuilder;
	@Mock
	MessagesRequestBuilder messagesRequestBuilder;
	@Mock
	private GraphServiceClient graphServiceClient;
	@Mock
	private UsersRequestBuilder usersRequestBuilder;
	@Mock
	private UserItemRequestBuilder userItemRequestBuilder;
	@Mock
	private MailFoldersRequestBuilder mailFoldersRequestBuilder;
	@InjectMocks
	private GraphClient graphClient;

	@Test
	void testInitializeGraph() {
		// Arrange
		final var tokenCredential = mock(TokenCredential.class);
		final var graphScopes = new String[] {
			"https://graph.microsoft.com/.default"
		};

		// Act
		graphClient.initializeGraph(tokenCredential, graphScopes);

		// Assert
		assertThat(graphServiceClient).isNotNull();
	}

	@Test
	void testGetInbox() {
		// Arrange
		final var userId = "userId";
		final var folderId = "folderId";
		final var messageCollectionResponse = mock(MessageCollectionResponse.class);

		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenReturn(userItemRequestBuilder);
		when(userItemRequestBuilder.mailFolders()).thenReturn(mailFoldersRequestBuilder);
		when(mailFoldersRequestBuilder.byMailFolderId(folderId)).thenReturn(mailFolderItemRequestBuilder);
		when(mailFolderItemRequestBuilder.messages()).thenReturn(messagesRequestBuilder);
		when(messagesRequestBuilder.get()).thenReturn(messageCollectionResponse);

		// Act
		final var result = graphClient.getInbox(userId, folderId);

		// Assert
		assertThat(result).isNotNull().isEqualTo(messageCollectionResponse);
	}
}
