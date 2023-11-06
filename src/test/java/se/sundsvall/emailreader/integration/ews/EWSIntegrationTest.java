package se.sundsvall.emailreader.integration.ews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
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


@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class EWSIntegrationTest {

	@InjectMocks
	private EWSIntegration ewsIntegration;

	private ExchangeService mockedService;

	private EWSMapper mapper;

	@BeforeEach
	public void setUp() throws Exception {

		mockedService = mock(ExchangeService.class);
		final var serviceField = ewsIntegration.getClass().getDeclaredField("service");
		serviceField.setAccessible(true);
		serviceField.set(ewsIntegration, mockedService);

		mapper = mock(EWSMapper.class);
		final var mapperField = ewsIntegration.getClass().getDeclaredField("mapper");
		mapperField.setAccessible(true);
		mapperField.set(ewsIntegration, mapper);

	}

	@Test
	void pageThroughEntireInbox() throws Exception {

		final var findItemsResults = setUpFindItemsResults();

		when(mockedService.findItems(any(FolderId.class),
			any()))
			.thenReturn(findItemsResults);

		when(mapper.toEmail(
			any(EmailMessage.class)))
			.thenReturn(createEmail());

		final var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress");

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.get(0).sender()).isEqualTo("someSender");
		assertThat(result.get(0).subject()).isEqualTo("someSubject");
		assertThat(result.get(0).message()).isEqualTo("someMessage");
		assertThat(result.get(0).id()).isNotEmpty();
		assertThat(result.get(0).recipients()).hasSize(1).satisfies(recipient -> {
			assertThat(recipient.get(0)).isEqualTo("someRecipient");
		});
		assertThat(result.get(0).attachments()).hasSize(1).satisfies(attachment -> {
			assertThat(attachment.get(0).contentType()).isEqualTo("someContentType");
			assertThat(attachment.get(0).name()).isEqualTo("someName");
			assertThat(attachment.get(0).content()).isEqualTo("someContent");
		});

	}

	@Test
	void pageThroughEntireInbox_cantFindItems(final CapturedOutput output) throws Exception {

		when(mockedService.findItems(any(FolderId.class),
			any()))
			.thenThrow(new HttpErrorException("Some cool error message from the server", 401));

		final var result = ewsIntegration.pageThroughEntireInbox(
			"someUsername", "somePassword", "someDomain", "someEmailAdress");

		assertThat(result).isNotNull().isEmpty();
		assertThat(output).contains("Could not find items")
			.contains("microsoft.exchange.webservices.data.core.exception.http.HttpErrorException: Some cool error message from the server");
	}


	@Test
	void pageThroughEntireInbox_cantFindFolder(final CapturedOutput output) throws Exception {

		when(mockedService.findFolders(any(FolderId.class),
			any(SearchFilter.class), any(FolderView.class)))
			.thenReturn(new FindFoldersResults());

		final var itemId = new ItemId("123");

		assertThatThrownBy(() -> ewsIntegration.moveEmail(itemId, "someEmailAdress", "someFolder"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Could not determine a unique folder with the name: someFolder");
	}

	@Test
	void testMoveEmail() throws Exception {

		when(mockedService.getRequestedServerVersion())
			.thenReturn(ExchangeVersion.Exchange2010_SP2);

		final var findFolderResults = setUpFindFolderResults();
		when(mockedService.findFolders(any(FolderId.class),
			any(SearchFilter.class), any(FolderView.class)))
			.thenReturn(findFolderResults);

		final var email = mock(EmailMessage.class);

		when(mockedService.bindToItem(any(ItemId.class), any()))
			.thenReturn(email);

		ewsIntegration.moveEmail(new ItemId("12123"), "someEmailAdress", "someFolder");

		verify(mockedService, times(1)).bindToItem(any(ItemId.class), any());
		verifyNoMoreInteractions(mockedService);
	}

	private FindFoldersResults setUpFindFolderResults() throws Exception {

		final var findFolderResults = new FindFoldersResults();
		findFolderResults.setTotalCount(1);
		findFolderResults.getFolders().add(new Folder(mockedService));

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

}
