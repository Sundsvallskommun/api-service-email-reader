package se.sundsvall.emailreader.integration.ews;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.Mailbox;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;
import okhttp3.internal.connection.Exchange;

/**
 * Exchange Web Services Integration
 */
@Service
@CircuitBreaker(name = "EWSIntegration")
public class EWSIntegration {


	private final EWSMapper mapper = new EWSMapper();

	private final FolderView folderView = new FolderView(10);

	private final ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

	private final Logger log = LoggerFactory.getLogger(EWSIntegration.class);

	private final PropertySet propertySetTextBody = new PropertySet(BasePropertySet.FirstClassProperties,
		ItemSchema.Body);

	public EWSIntegration() {
		this.propertySetTextBody.setRequestedBodyType(BodyType.Text);
	}

	public List<Email> pageThroughEntireInbox(final String username, final String password, final String domain, final String emailAddress) {

		// These properties should be replaced with credentials from the database in a later step
		this.service.setCredentials(new WebCredentials(username, password));
		this.service.setUrl(URI.create(domain));

		final var emails = new ArrayList<Email>();

		final var pageSize = 50;
		final var view = new ItemView(pageSize);


		FindItemsResults<Item> findResults;
		final var userMailbox = new Mailbox(emailAddress);
		final var folderId = new FolderId(WellKnownFolderName.Inbox, userMailbox);

		do {
			try {
				findResults = service.findItems(folderId, view);
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
					}
				} catch (final Exception e) {
					log.error("Could not load message", e);
				}
			});

			view.setOffset(view.getOffset() + pageSize);

		} while (findResults.isMoreAvailable());

		return emails;
	}

	public void moveEmail(final ItemId emailId, final String emailAddress, final String folderName) throws Exception {

		final Folder destinationFolder;

		destinationFolder = findFolder(emailAddress, folderName);

		final var email = service.bindToItem(emailId, new PropertySet());

		if (email instanceof final EmailMessage message) {
			message.setIsRead(true);
			message.update(ConflictResolutionMode.AutoResolve);
			message.move(destinationFolder.getId());
		}
	}

	private Folder findFolder(final String emailAddress, final String folderName) throws Exception {

		final var userMailbox = new Mailbox(emailAddress);
		final var folderId = new FolderId(WellKnownFolderName.MsgFolderRoot, userMailbox);

		// Max number of folders to retrieve
		folderView.setPropertySet(new PropertySet(BasePropertySet.IdOnly, FolderSchema.DisplayName));

		final var searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
		final var findFoldersResults = service.findFolders(folderId, searchFilter, folderView);

		if (findFoldersResults == null || findFoldersResults.getFolders().size() != 1) {
			throw new IllegalArgumentException("Could not determine a unique folder with the name: " + folderName);
		}

		return findFoldersResults.getFolders().getFirst();
	}

}
