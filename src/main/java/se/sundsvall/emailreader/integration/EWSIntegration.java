package se.sundsvall.emailreader.integration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;


@Service
public class EWSIntegration {

    private final EWSProperties properties;

    private final EWSMapper mapper = new EWSMapper();

    private final FolderView folderView = new FolderView(10);

    private final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

    private final Logger log = LoggerFactory.getLogger(EWSIntegration.class);

    private final PropertySet propertySetTextBody = new PropertySet(BasePropertySet.FirstClassProperties,
        ItemSchema.Body);

    public EWSIntegration(final EWSProperties properties) {
        this.properties = properties;
        this.propertySetTextBody.setRequestedBodyType(BodyType.Text);
    }

    public List<Email> pageThroughEntireInbox(final String requestedDestinationFolder) {

        // These properties should be replaced with credentials from the database in a later step
        this.service.setCredentials(new WebCredentials(properties.username(), properties.password()));
        this.service.setUrl(URI.create(properties.url()));

        final var emails = new ArrayList<Email>();

        final var pageSize = 50;
        final var view = new ItemView(pageSize);
        final Folder destinationFolder;

        try {
            destinationFolder = findFolder(service, requestedDestinationFolder);
        } catch (final Exception e) {
            log.error("Could not find destination folder", e);
            return emails;
        }

        FindItemsResults<Item> findResults;

        do {
            try {
                findResults = service.findItems(WellKnownFolderName.Inbox, view);
            } catch (final Exception e) {
                log.error("Could not find items", e);
                return emails;
            }
            findResults.getItems().forEach(item -> {
                try {
                    if (item instanceof final EmailMessage message) {
                        message.load(); // Load the full message data
                        service.loadPropertiesForItems(List.of(message), propertySetTextBody);
                        emails.add(mapper.toEmail(message));
                        message.move(destinationFolder.getId());
                    }
                } catch (final Exception e) {
                    log.error("Could not load message", e);
                }
            });

            view.setOffset(view.getOffset() + pageSize);

        } while (findResults.isMoreAvailable());

        return emails;
    }

    private Folder findFolder(final ExchangeService service, final String folderName) throws Exception {

        // Max number of folders to retrieve
        folderView.setPropertySet(new PropertySet(BasePropertySet.IdOnly, FolderSchema.DisplayName));

        final var searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
        final var findFoldersResults = service.findFolders(WellKnownFolderName.MsgFolderRoot, searchFilter, folderView);

        if (findFoldersResults == null || findFoldersResults.getFolders().size() != 1) {
            throw new IllegalArgumentException("Could not determine a unique folder with the name: " + folderName);
        }

        return findFoldersResults.getFolders().get(0);
    }

}
