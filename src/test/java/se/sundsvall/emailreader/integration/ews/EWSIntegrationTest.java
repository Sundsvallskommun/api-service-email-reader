package se.sundsvall.emailreader.integration.ews;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.http.HttpErrorException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
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
    public void setUp() throws IllegalAccessException, NoSuchFieldException {

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

        when(mockedService.getRequestedServerVersion())
            .thenReturn(ExchangeVersion.Exchange2010_SP2);

        final var findItemsResults = setUpFindItemsResults();
        final var findFolderResults = setUpFindFolderResults();

        when(mockedService.findItems(any(WellKnownFolderName.class),
            any()))
            .thenReturn(findItemsResults);

        when(mockedService.findFolders(any(WellKnownFolderName.class),
            any(SearchFilter.class), any(FolderView.class)))
            .thenReturn(findFolderResults);

        when(mapper.toEmail(
            any(EmailMessage.class)))
            .thenReturn(createEmail());

        final var result = ewsIntegration.pageThroughEntireInbox(
            "someFolder", "someUsername", "somePassword", "someDomain");

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).from()).isEqualTo("someFrom");
        assertThat(result.get(0).subject()).isEqualTo("someSubject");
        assertThat(result.get(0).message()).isEqualTo("someMessage");
        assertThat(result.get(0).id()).isNotEmpty();
        assertThat(result.get(0).to()).hasSize(1).satisfies(to -> {
            assertThat(to.get(0)).isEqualTo("someTo");
        });
        assertThat(result.get(0).attachments()).hasSize(1).satisfies(attachment -> {
            assertThat(attachment.get(0).contentType()).isEqualTo("someContentType");
            assertThat(attachment.get(0).name()).isEqualTo("someName");
            assertThat(attachment.get(0).content()).isEqualTo("someContent");
        });

    }

    @Test
    void pageThroughEntireInbox_cantFindFolder(final CapturedOutput output) throws Exception {

        when(mockedService.findFolders(any(WellKnownFolderName.class),
            any(SearchFilter.class), any(FolderView.class)))
            .thenReturn(new FindFoldersResults());

        final var result = ewsIntegration.pageThroughEntireInbox(
            "someFolder", "someUsername", "somePassword", "someDomain");

        assertThat(result).isNotNull().isEmpty();
        assertThat(output).contains("Could not find destination folder")
            .contains("java.lang.IllegalArgumentException: Could not determine a unique folder with the name: someFolder");

    }

    @Test
    void pageThroughEntireInbox_cantFindItems(final CapturedOutput output) throws Exception {

        when(mockedService.getRequestedServerVersion())
            .thenReturn(ExchangeVersion.Exchange2010_SP2);

        final var findFolderResults = setUpFindFolderResults();

        when(mockedService.findFolders(any(WellKnownFolderName.class),
            any(SearchFilter.class), any(FolderView.class)))
            .thenReturn(findFolderResults);

        when(mockedService.findItems(any(WellKnownFolderName.class),
            any()))
            .thenThrow(new HttpErrorException("Some cool error message from the server", 401));

        final var result = ewsIntegration.pageThroughEntireInbox(
            "someFolder", "someUsername", "somePassword", "someDomain");

        assertThat(result).isNotNull().isEmpty();
        assertThat(output).contains("Could not find items")
            .contains("microsoft.exchange.webservices.data.core.exception.http.HttpErrorException: Some cool error message from the server");
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
