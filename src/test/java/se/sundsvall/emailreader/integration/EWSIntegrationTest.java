package se.sundsvall.emailreader.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import se.sundsvall.emailreader.api.model.Email;

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

    @Mock
    EWSProperties properties;

    @InjectMocks
    private EWSIntegration ewsIntegration;

    private ExchangeService mockedService;

    @Mock
    private EWSMapper mapper;

    @BeforeEach
    public void setUp() throws IllegalAccessException {

        mockedService = mock(ExchangeService.class);

        final Field field = ReflectionUtils
            .findFields(EWSIntegration.class, f -> f.getName().equals("service"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);

        field.setAccessible(true);
        field.set(ewsIntegration, mockedService);

        when(properties.username()).thenReturn("username");
        when(properties.password()).thenReturn("password");
        when(properties.url()).thenReturn("https://epost.dummy.se/EWS/Exchange.asmx");
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


        when(mapper.toEmail(any(ExchangeService.class),
            any(EmailMessage.class)))
            .thenReturn(new Email(List.of("Test testorsson"), "Testy testorsson",
                "someSubject", "someBody", "someId"));

        final var result = ewsIntegration.pageThroughEntireInbox("someFolder");

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).from()).isEqualTo("Testy testorsson");
        assertThat(result.get(0).subject()).isEqualTo("someSubject");
        assertThat(result.get(0).message()).isEqualTo("someBody");
        assertThat(result.get(0).id()).isNotEmpty();

    }

    @Test
    void pageThroughEntireInbox_cantFindFolder(final CapturedOutput output) throws Exception {

        when(mockedService.findFolders(any(WellKnownFolderName.class),
            any(SearchFilter.class), any(FolderView.class)))
            .thenReturn(new FindFoldersResults());

        final var result = ewsIntegration.pageThroughEntireInbox("someFolder");

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

        final var result = ewsIntegration.pageThroughEntireInbox("someFolder");

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
