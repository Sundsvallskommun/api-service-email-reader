package se.sundsvall.emailreader.integration.ews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class EWSIntegrationTest {

	private static final String EMAIL_MESSAGE_BODY_TEXT = """
		CustomerId = 123456789
		User = testUser
		Password = testPassword
		Sender = user@host.com
		Message = Nytt lösenord driftkonto: nyttLösenord
		Recipient = 070123456789
		""";

	private ExchangeService exchangeServiceMock;

	private EWSIntegration ewsIntegration;

	@Mock
	private EmailMessage emailMessageMock;

	@Mock
	private MessageBody messageBodyMock;

	@Mock
	private Consumer<String> consumerMock;

	private static Stream<Arguments> recipientNumbersProvider() {
		return Stream.of(
			Arguments.of(Map.of("Recipient", "0713266789"), Map.of("VALID", List.of("+46713266789"))),
			Arguments.of(Map.of("Recipient", "0721232179,03ab2"), Map.of("VALID", List.of("+46721232179"), "INVALID", List.of("+463ab2"))),
			Arguments.of(Map.of("Recipient", "0791234889,070213456789,12332ab,023-112"), Map.of("VALID", List.of("+46791234889", "+4670213456789"), "INVALID", List.of("12332ab", "+4623-112"))));
	}

	@BeforeEach
	void setUp() throws Exception {
		ewsIntegration = new EWSIntegration();
		exchangeServiceMock = mock(ExchangeService.class);
		final var serviceField = ewsIntegration.getClass().getDeclaredField("exchangeService");
		serviceField.setAccessible(true);
		serviceField.set(ewsIntegration, exchangeServiceMock);
		ReflectionTestUtils.setField(ewsIntegration, "maxFileSize", "10485760"); // Set max file size to 10 MB
	}

	@Test
	void pageThroughEntireInbox() throws Exception {
		final var findItemsResults = setUpFindItemsResults();
		final var emailMessages = findItemsResults.getItems();

		when(exchangeServiceMock.findItems(any(FolderId.class), any())).thenReturn(findItemsResults);

		final var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress", consumerMock);

		assertThat(result).isNotNull().hasSize(1).isEqualTo(emailMessages);
	}

	@Test
	void pageThroughEntireInbox_cantFindItems(final CapturedOutput output) throws Exception {
		when(exchangeServiceMock.findItems(any(FolderId.class), any()))
			.thenThrow(new HttpErrorException("Some cool error message from the server", 401));

		final var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress", consumerMock);

		assertThat(result).isNotNull().isEmpty();
		assertThat(output).contains("Could not find items")
			.contains("microsoft.exchange.webservices.data.core.exception.http.HttpErrorException: Some cool error message from the server");
	}

	@Test
	void pageThroughEntireInbox_cantFindFolder() throws Exception {
		// Arrange
		final var itemId = new ItemId("123");

		when(exchangeServiceMock.findFolders(any(FolderId.class), any(SearchFilter.class), any(FolderView.class)))
			.thenReturn(new FindFoldersResults());
		when(exchangeServiceMock.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);

		// Act
		ewsIntegration.moveEmail(itemId, "someEmailAdress", "someFolder");

		// Assert
		verify(exchangeServiceMock, times(1)).bindToItem(any(ItemId.class), any());
		verify(exchangeServiceMock, times(1))
			.findFolders(any(FolderId.class), any(SearchFilter.class), any(FolderView.class));
		verify(exchangeServiceMock).createFolder(any(Folder.class), any());
		verifyNoMoreInteractions(exchangeServiceMock);
	}

	@Test
	void testMoveEmail() throws Exception {
		when(exchangeServiceMock.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);
		final var findFolderResults = setUpFindFolderResults();

		when(exchangeServiceMock.findFolders(any(FolderId.class), any(SearchFilter.class), any(FolderView.class))).thenReturn(findFolderResults);
		final var email = mock(EmailMessage.class);

		when(exchangeServiceMock.bindToItem(any(ItemId.class), any())).thenReturn(email);

		ewsIntegration.moveEmail(new ItemId("12123"), "someEmailAdress", "someFolder");

		verify(exchangeServiceMock, times(1)).bindToItem(any(ItemId.class), any());
		verifyNoMoreInteractions(exchangeServiceMock);
	}

	@Test
	void testDeleteEmail() throws Exception {
		final var emailId = new ItemId("123");
		final var emailMessage = mock(EmailMessage.class);

		when(exchangeServiceMock.bindToItem(any(ItemId.class), any(PropertySet.class))).thenReturn(emailMessage);

		ewsIntegration.deleteEmail(emailId);

		verify(emailMessage).delete(DeleteMode.HardDelete);
	}

	private FindFoldersResults setUpFindFolderResults() throws Exception {
		final var findFolderResults = new FindFoldersResults();
		findFolderResults.setTotalCount(1);
		findFolderResults.getFolders().add(new Folder(exchangeServiceMock));

		return findFolderResults;
	}

	private FindItemsResults<Item> setUpFindItemsResults() throws Exception {
		final var emailMessage = mock(EmailMessage.class);
		emailMessage.setFrom(new EmailAddress("Test testorsson", "test@test.se"));
		emailMessage.setSubject("someSubject");
		emailMessage.setBody(new MessageBody("someBody"));

		final var findItemsResults = new FindItemsResults<>();
		findItemsResults.getItems().add(emailMessage);

		return findItemsResults;
	}

	@Test
	void extractValuesEmailMessage() throws ServiceLocalException {
		when(emailMessageMock.getBody()).thenReturn(messageBodyMock);
		when(messageBodyMock.toString()).thenReturn(EMAIL_MESSAGE_BODY_TEXT);

		final var resultMap = ewsIntegration.extractValuesEmailMessage(emailMessageMock);

		assertThat(resultMap).isNotNull()
			.containsEntry("Sender", "user@host.com")
			.containsEntry("Message", "Nytt lösenord driftkonto: nyttLösenord")
			.containsEntry("Recipient", "070123456789");

		verify(emailMessageMock).getBody();
	}

	@ParameterizedTest
	@MethodSource("recipientNumbersProvider")
	void validateRecipientNumbers(final Map<String, String> input, final Map<String, List<String>> expected) {

		final var result = ewsIntegration.validateRecipientNumbers(input);

		assertThat(result).isNotNull();
		assertThat(result.get("VALID")).isEqualTo(expected.get("VALID"));
		assertThat(result.get("INVALID")).isEqualTo(expected.get("INVALID"));
		assertThat(result).usingRecursiveComparison().isEqualTo(expected);
	}

	@Test
	void loadMessage() throws Exception {

		final var result = ewsIntegration.loadMessage(emailMessageMock, consumerMock);

		assertThat(result).isNotNull().isEqualTo(emailMessageMock);
		verify(emailMessageMock).load();
		verify(exchangeServiceMock).loadPropertiesForItems(eq(List.of(emailMessageMock)), any(PropertySet.class));
	}

	@Test
	void loadMessageThrowsException() throws Exception {
		doThrow(ServiceLocalException.class).when(emailMessageMock).load();

		final var result = ewsIntegration.loadMessage(emailMessageMock, consumerMock);

		assertThat(result).isNull();
		verify(consumerMock).accept("[EWS] Could not load message");
		verify(emailMessageMock).load();
	}

}
