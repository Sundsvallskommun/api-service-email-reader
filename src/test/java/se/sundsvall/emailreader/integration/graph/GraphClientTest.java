package se.sundsvall.emailreader.integration.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.AttachmentCollectionResponse;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.MessageCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.UsersRequestBuilder;
import com.microsoft.graph.users.item.UserItemRequestBuilder;
import com.microsoft.graph.users.item.mailfolders.MailFoldersRequestBuilder;
import com.microsoft.graph.users.item.mailfolders.item.MailFolderItemRequestBuilder;
import com.microsoft.graph.users.item.messages.MessagesRequestBuilder;
import com.microsoft.graph.users.item.messages.item.MessageItemRequestBuilder;
import com.microsoft.graph.users.item.messages.item.attachments.AttachmentsRequestBuilder;
import com.microsoft.graph.users.item.messages.item.move.MovePostRequestBody;
import com.microsoft.graph.users.item.messages.item.move.MoveRequestBuilder;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphClientTest {

	@Mock
	private com.microsoft.graph.users.item.mailfolders.item.messages.item.MessageItemRequestBuilder mailfolderMessageItemRequestBuilder;
	@Mock
	private com.microsoft.graph.users.item.mailfolders.item.messages.item.move.MoveRequestBuilder mailfolderMoveRequestBuilder;
	@Mock
	private MailFolderItemRequestBuilder mailFolderItemRequestBuilder;
	@Mock
	private MessagesRequestBuilder messagesRequestBuilder;
	@Mock
	private com.microsoft.graph.users.item.mailfolders.item.messages.MessagesRequestBuilder mailfolderMessagesRequestBuilder;
	@Mock
	private GraphServiceClient graphServiceClient;
	@Mock
	private UsersRequestBuilder usersRequestBuilder;
	@Mock
	private UserItemRequestBuilder userItemRequestBuilder;
	@Mock
	private MailFoldersRequestBuilder mailFoldersRequestBuilder;
	@Mock
	private MessageItemRequestBuilder messageItemRequestBuilder;
	@Mock
	private AttachmentsRequestBuilder attachmentsRequestBuilder;
	@Mock
	private AttachmentCollectionResponse attachmentCollectionResponse;
	@Mock
	private MoveRequestBuilder moveRequestBuilder;
	@InjectMocks
	private GraphClient graphClient;

	@Mock
	private Consumer<String> consumerMock;

	@Test
	void testInitializeGraph() {
		// Arrange
		final var tokenCredential = mock(TokenCredential.class);

		// Act
		graphClient.initializeGraph(tokenCredential, consumerMock);

		// Assert
		assertThat(graphServiceClient).isNotNull();
	}

	@Test
	void testGetInbox() {
		// Arrange
		final var userId = "userId";
		final var folderId = "inbox";
		final var messageCollectionResponse = mock(MessageCollectionResponse.class);
		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenReturn(userItemRequestBuilder);
		when(userItemRequestBuilder.mailFolders()).thenReturn(mailFoldersRequestBuilder);
		when(mailFoldersRequestBuilder.byMailFolderId(folderId)).thenReturn(mailFolderItemRequestBuilder);
		when(mailFolderItemRequestBuilder.messages()).thenReturn(mailfolderMessagesRequestBuilder);
		when(mailfolderMessagesRequestBuilder.get(any())).thenReturn(messageCollectionResponse);

		// Act
		final var result = graphClient.getInbox(userId, consumerMock);

		// Assert
		assertThat(result).isNotNull().isEqualTo(messageCollectionResponse);
	}

	@Test
	void testGetInboxThrowsException() {
		// Arrange
		final var userId = "userId";

		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenThrow(new RuntimeException("Test exception"));

		// Act
		final var result = graphClient.getInbox(userId, consumerMock);

		// Assert
		assertThat(result).isNull();
		verify(consumerMock).accept("[GRAPH] Could not get inbox");
	}

	@Test
	void testGetAttachments() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";
		final var attachments = List.of(new Attachment());
		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenReturn(userItemRequestBuilder);
		when(userItemRequestBuilder.messages()).thenReturn(messagesRequestBuilder);
		when(messagesRequestBuilder.byMessageId(messageId)).thenReturn(messageItemRequestBuilder);
		when(messageItemRequestBuilder.attachments()).thenReturn(attachmentsRequestBuilder);
		when(attachmentsRequestBuilder.get()).thenReturn(attachmentCollectionResponse);
		when(attachmentCollectionResponse.getValue()).thenReturn(attachments);

		// Act
		final var result = graphClient.getAttachments(userId, messageId, consumerMock);

		// Assert
		assertThat(result).isNotNull().isEqualTo(attachments);
	}

	@Test
	void testGetAttachmentsThrowsException() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";

		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenThrow(new RuntimeException("Test exception"));

		// Act
		final var result = graphClient.getAttachments(userId, messageId, consumerMock);

		// Assert
		assertThat(result).isEmpty();
		verify(consumerMock).accept("[GRAPH] not get attachments for email");
	}

	@Test
	void testMoveEmail() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";
		final var destinationFolder = "destinationFolder";
		final var request = new MovePostRequestBody();
		request.setDestinationId(destinationFolder);
		final var test = new Message();

		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenReturn(userItemRequestBuilder);
		when(userItemRequestBuilder.mailFolders()).thenReturn(mailFoldersRequestBuilder);
		when(mailFoldersRequestBuilder.byMailFolderId("inbox")).thenReturn(mailFolderItemRequestBuilder);
		when(mailFolderItemRequestBuilder.messages()).thenReturn(mailfolderMessagesRequestBuilder);
		when(mailfolderMessagesRequestBuilder.byMessageId(any())).thenReturn(mailfolderMessageItemRequestBuilder);
		when(mailfolderMessageItemRequestBuilder.move()).thenReturn(mailfolderMoveRequestBuilder);
		when(mailfolderMoveRequestBuilder.post(any())).thenReturn(test);

		// Act
		graphClient.moveEmail(userId, messageId, destinationFolder, consumerMock);

		// Assert
		verify(mailfolderMoveRequestBuilder).post(any());
	}

	@Test
	void testMoveEmailThrowsException() {
		// Arrange
		final var userId = "userId";
		final var messageId = "messageId";
		final var destinationFolder = "destinationFolder";

		when(graphServiceClient.users()).thenReturn(usersRequestBuilder);
		when(usersRequestBuilder.byUserId(userId)).thenThrow(new RuntimeException("Test exception"));

		// Act
		graphClient.moveEmail(userId, messageId, destinationFolder, consumerMock);

		// Assert
		verify(consumerMock).accept("[GRAPH] Could not move email from inbox");
	}
}
