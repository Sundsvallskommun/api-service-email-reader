package se.sundsvall.emailreader.integration.ews;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.sundsvall.dept44.common.validators.annotation.impl.ValidMSISDNConstraintValidator;

import static java.util.Collections.emptyMap;
import static microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode.HardDelete;

/**
 * Exchange Web Services Integration
 */
@Service
@CircuitBreaker(name = "EWSIntegration")
public class EWSIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EWSIntegration.class);
	private static final List<String> SMS_MAIL_MESSAGE_KEYS_TO_PARSE = List.of("Message", "Recipient", "Sender");
	private static final String COULD_NOT_LOAD_MESSAGE = "Could not load message";
	private final FolderView folderView = new FolderView(10);
	private final ExchangeService exchangeService = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
	private final PropertySet propertySetTextBody = new PropertySet(BasePropertySet.FirstClassProperties, ItemSchema.Body);
	private final PropertySet propertySetHTMLBody = new PropertySet(BasePropertySet.FirstClassProperties, ItemSchema.Body);
	@Value("${scheduled.check-for-new-emails.ews.maxFileSize:10485760}") // Default to 10 MB
	private String maxFileSize;

	public EWSIntegration() {
		this.propertySetTextBody.setRequestedBodyType(BodyType.Text);
		this.propertySetHTMLBody.setRequestedBodyType(BodyType.HTML);
	}

	public List<EmailMessage> pageThroughEntireInbox(final String username, final String password, final String domain, final String emailAddress, final Consumer<String> setUnHealthyConsumer) {

		// These properties should be replaced with credentials from the database in a later step
		exchangeService.setCredentials(new WebCredentials(username, password));
		exchangeService.setUrl(URI.create(domain));

		final var emails = new ArrayList<EmailMessage>();

		final var pageSize = 50;
		final var view = new ItemView(pageSize);

		FindItemsResults<Item> findResults;
		final var userMailbox = new Mailbox(emailAddress);
		final var folderId = new FolderId(WellKnownFolderName.Inbox, userMailbox);

		do {
			try {
				findResults = exchangeService.findItems(folderId, view);
			} catch (final Exception e) {
				setUnHealthyConsumer.accept("[EWS] Could not find items, address: " + emailAddress);
				LOG.error("Could not find items", e);
				return emails;
			}
			findResults.getItems().forEach(item -> {
				try {
					if (item instanceof final EmailMessage message) {
						if (message.getSize() > Long.parseLong(maxFileSize)) {
							setUnHealthyConsumer.accept("[EWS] Email size exceeds maximum allowed size for address: " + emailAddress);
							LOG.warn("Skipping email with size {} bytes, larger than max allowed size of {} bytes for address {}", message.getSize(), maxFileSize, emailAddress);
							return;
						}
						emails.add(message);
					}
				} catch (final Exception e) {
					setUnHealthyConsumer.accept("[EWS] Could not load message for address: " + emailAddress);
					LOG.error(COULD_NOT_LOAD_MESSAGE, e);
				}
			});

			view.setOffset(view.getOffset() + pageSize);

		} while (findResults.isMoreAvailable());
		return emails;
	}

	public void moveEmail(final ItemId emailId, final String emailAddress, final String folderName) throws Exception {

		final Folder destinationFolder;

		destinationFolder = findFolder(emailAddress, folderName);

		final var email = exchangeService.bindToItem(emailId, new PropertySet());

		if (email instanceof final EmailMessage message) {
			message.setIsRead(true);
			message.update(ConflictResolutionMode.AutoResolve);
			message.move(destinationFolder.getId());
		}
	}

	public void deleteEmail(final ItemId emailId) throws Exception {
		final var email = exchangeService.bindToItem(emailId, new PropertySet());
		if (email instanceof final EmailMessage message) {
			message.delete(HardDelete);
		}
	}

	private Folder findFolder(final String emailAddress, final String folderName) throws Exception {

		final var userMailbox = new Mailbox(emailAddress);
		final var folderId = new FolderId(WellKnownFolderName.MsgFolderRoot, userMailbox);

		// Max number of folders to retrieve
		folderView.setPropertySet(new PropertySet(BasePropertySet.IdOnly, FolderSchema.DisplayName));

		final var searchFilter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);
		final var findFoldersResults = exchangeService.findFolders(folderId, searchFilter, folderView);

		if ((findFoldersResults != null) && (findFoldersResults.getFolders().size() > 1)) {
			throw new IllegalArgumentException("Could not determine a unique folder with the name: " + folderName);
		}

		if ((findFoldersResults == null) || (findFoldersResults.getFolders().isEmpty())) {
			final var newFolder = new Folder(exchangeService);
			newFolder.setDisplayName(folderName);
			exchangeService.createFolder(newFolder, folderId);
			return newFolder;
		}

		return findFoldersResults.getFolders().getFirst();
	}

	public EmailMessage loadMessage(final EmailMessage emailMessage, final Consumer<String> setUnHealthyConsumer) {

		try {
			emailMessage.load(); // Load the full message data
			exchangeService.loadPropertiesForItems(List.of(emailMessage), propertySetTextBody);
			return emailMessage;
		} catch (final Exception e) {
			setUnHealthyConsumer.accept("[EWS] " + COULD_NOT_LOAD_MESSAGE);
			LOG.error(COULD_NOT_LOAD_MESSAGE, e);
		}
		return null;
	}

	public EmailMessage loadHTMLMessage(final EmailMessage emailMessage, final Consumer<String> setUnHealthyConsumer) {

		try {
			emailMessage.load(); // Load the full message data
			exchangeService.loadPropertiesForItems(List.of(emailMessage), propertySetHTMLBody);
			return emailMessage;
		} catch (final Exception e) {
			setUnHealthyConsumer.accept("[EWS] " + COULD_NOT_LOAD_MESSAGE);
			LOG.error(COULD_NOT_LOAD_MESSAGE, e);
		}
		return null;
	}

	public Map<String, String> extractValuesEmailMessage(final EmailMessage emailMessage) {
		try {

			emailMessage.load();
			exchangeService.loadPropertiesForItems(List.of(emailMessage), propertySetTextBody);

			return Arrays.stream(emailMessage.getBody().toString().split("\n"))
				.map(line -> line.split("=", 2))
				.filter(pairs -> pairs.length == 2)
				.filter(pairs -> SMS_MAIL_MESSAGE_KEYS_TO_PARSE.stream().anyMatch(key -> key.equalsIgnoreCase(pairs[0].trim())))
				.collect(Collectors.toMap(
					pairs -> pairs[0].trim(),
					pairs -> pairs[1].trim()));
		} catch (final Exception e) {
			LOG.error("Exception in extractValuesEmailMessage method", e);
			return emptyMap();
		}
	}

	public Map<String, List<String>> validateRecipientNumbers(final Map<String, String> keyValueMap) {
		final var commaSeparatedNumbers = keyValueMap.get("Recipient");
		final var validationMap = new HashMap<String, List<String>>();
		final var numbers = Arrays.asList(commaSeparatedNumbers.split(","));
		final var formattedNumbers = numbers.stream()
			.map(number -> number.replace("-", ""))
			.map(number -> {
				if (number.startsWith("0")) {
					return number.replaceFirst("^0", "+46");
				}
				return number;
			})
			.toList();
		final var validator = new ValidMSISDNConstraintValidator();

		for (final var number : formattedNumbers) {
			if (validator.isValid(number)) {
				validationMap.computeIfAbsent("VALID", k -> new ArrayList<>());
				validationMap.get("VALID").add(number);
			} else {
				validationMap.computeIfAbsent("INVALID", k -> new ArrayList<>());
				validationMap.get("INVALID").add(number);
			}
		}

		return validationMap;
	}

}
