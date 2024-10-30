package se.sundsvall.emailreader.integration.ews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class EWSIntegrationTest {

	private static final String EMAIL_MESSAGE_BODY_TEXT = """
		CustomerId = 123456789
		User = testUser
		Password = testPassword
		Message = Nytt lösenord driftkonto: nyttLösenord
		Recipient = 070123456789
		""";

	private ExchangeService exchangeServiceMock;

	private EWSIntegration ewsIntegration;

	@Mock
	private EmailMessage emailMessageMock;

	@Mock
	private MessageBody messageBodyMock;

	@BeforeEach
	public void setUp() throws Exception {
		ewsIntegration = new EWSIntegration();
		exchangeServiceMock = mock(ExchangeService.class);
		var serviceField = ewsIntegration.getClass().getDeclaredField("exchangeService");
		serviceField.setAccessible(true);
		serviceField.set(ewsIntegration, exchangeServiceMock);
	}

	@Test
	void pageThroughEntireInbox() throws Exception {
		var findItemsResults = setUpFindItemsResults();
		var emailMessages = findItemsResults.getItems();

		when(exchangeServiceMock.findItems(any(FolderId.class), any())).thenReturn(findItemsResults);

		var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress");

		assertThat(result).isNotNull().hasSize(1).isEqualTo(emailMessages);
	}

	@Test
	void pageThroughEntireInbox_cantFindItems(final CapturedOutput output) throws Exception {
		when(exchangeServiceMock.findItems(any(FolderId.class), any()))
			.thenThrow(new HttpErrorException("Some cool error message from the server", 401));

		final var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress");

		assertThat(result).isNotNull().isEmpty();
		assertThat(output).contains("Could not find items")
			.contains("microsoft.exchange.webservices.data.core.exception.http.HttpErrorException: Some cool error message from the server");
	}

	@Test
	void pageThroughEntireInbox_cantFindFolder() throws Exception {
		when(exchangeServiceMock.findFolders(any(FolderId.class),
			any(SearchFilter.class), any(FolderView.class)))
			.thenReturn(new FindFoldersResults());

		final var itemId = new ItemId("123");

		assertThatThrownBy(() -> ewsIntegration.moveEmail(itemId, "someEmailAdress", "someFolder"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Could not determine a unique folder with the name: someFolder");
	}

	@Test
	void testMoveEmail() throws Exception {
		when(exchangeServiceMock.getRequestedServerVersion()).thenReturn(ExchangeVersion.Exchange2010_SP2);
		var findFolderResults = setUpFindFolderResults();

		when(exchangeServiceMock.findFolders(any(FolderId.class), any(SearchFilter.class), any(FolderView.class))).thenReturn(findFolderResults);
		var email = mock(EmailMessage.class);

		when(exchangeServiceMock.bindToItem(any(ItemId.class), any())).thenReturn(email);

		ewsIntegration.moveEmail(new ItemId("12123"), "someEmailAdress", "someFolder");

		verify(exchangeServiceMock, times(1)).bindToItem(any(ItemId.class), any());
		verifyNoMoreInteractions(exchangeServiceMock);
	}

	@Test
	void testDeleteEmail() throws Exception {
		var emailId = new ItemId("123");
		var emailMessage = mock(EmailMessage.class);

		when(exchangeServiceMock.bindToItem(any(ItemId.class), any(PropertySet.class))).thenReturn(emailMessage);

		ewsIntegration.deleteEmail(emailId);

		verify(emailMessage).delete(DeleteMode.HardDelete);
	}

	private FindFoldersResults setUpFindFolderResults() throws Exception {
		var findFolderResults = new FindFoldersResults();
		findFolderResults.setTotalCount(1);
		findFolderResults.getFolders().add(new Folder(exchangeServiceMock));

		return findFolderResults;
	}

	private FindItemsResults<Item> setUpFindItemsResults() throws Exception {
		var emailMessage = mock(EmailMessage.class);
		emailMessage.setFrom(new EmailAddress("Test testorsson", "test@test.se"));
		emailMessage.setSubject("someSubject");
		emailMessage.setBody(new MessageBody("someBody"));

		var findItemsResults = new FindItemsResults<>();
		findItemsResults.getItems().add(emailMessage);

		return findItemsResults;
	}

	@Test
	void extractValuesEmailMessage() throws ServiceLocalException {
		when(emailMessageMock.getBody()).thenReturn(messageBodyMock);
		when(messageBodyMock.toString()).thenReturn(EMAIL_MESSAGE_BODY_TEXT);

		var resultMap = ewsIntegration.extractValuesEmailMessage(emailMessageMock);

		assertThat(resultMap).isNotNull()
			.containsEntry("CustomerId", "123456789")
			.containsEntry("User", "testUser")
			.containsEntry("Password", "testPassword")
			.containsEntry("Message", "Nytt lösenord driftkonto: nyttLösenord")
			.containsEntry("Recipient", "070123456789");

		verify(emailMessageMock).getBody();
	}

	@ParameterizedTest
	@MethodSource("recipientNumbersProvider")
	void validateRecipientNumbers(final Map<String, String> input, final Map<String, List<String>> expected) {

		var result = ewsIntegration.validateRecipientNumbers(input);

		assertThat(result).isNotNull();
		assertThat(result.get("VALID")).isEqualTo(expected.get("VALID"));
		assertThat(result.get("INVALID")).isEqualTo(expected.get("INVALID"));
		assertThat(result).usingRecursiveComparison().isEqualTo(expected);
	}

	private static Stream<Arguments> recipientNumbersProvider() {
		return Stream.of(
			Arguments.of(Map.of("Recipient", "0713266789"), Map.of("VALID", List.of("+46713266789"))),
			Arguments.of(Map.of("Recipient", "0721232179,03ab2"), Map.of("VALID", List.of("+46721232179"), "INVALID", List.of("+463ab2"))),
			Arguments.of(Map.of("Recipient", "0791234889,070213456789,12332ab,023-112"), Map.of("VALID", List.of("+46791234889", "+4670213456789"), "INVALID", List.of("12332ab", "+4623-112"))));
	}

}
