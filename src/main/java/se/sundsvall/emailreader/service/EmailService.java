package se.sundsvall.emailreader.service;

import static java.text.MessageFormat.format;
import static se.sundsvall.emailreader.service.mapper.EmailMapper.toEmails;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.mapper.EmailMapper;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import microsoft.exchange.webservices.data.property.complex.ItemId;

@Service
public class EmailService {

	private static final String EMAIL_SUBJECT = "[Warning] EmailReader has detected unhandled emails";
	private static final String EMAIL_MESSAGE = "EmailReader has detected unhandled emails with the following IDs: {0}";

	private final EmailRepository emailRepository;
	private final CredentialsRepository credentialsRepository;

	private final MessagingIntegration messagingIntegration;
	private final EWSIntegration ewsIntegration;

	private final EncryptionUtility encryptionUtility;

	public EmailService(final EmailRepository emailRepository,
		final CredentialsRepository credentialsRepository,
		final MessagingIntegration messagingIntegration,
		final EWSIntegration ewsIntegration,
		final EncryptionUtility encryptionUtility) {
		this.emailRepository = emailRepository;
		this.credentialsRepository = credentialsRepository;
		this.messagingIntegration = messagingIntegration;
		this.ewsIntegration = ewsIntegration;
		this.encryptionUtility = encryptionUtility;
	}

	public List<Email> getAllEmails(final String municipalityId, final String namespace) {
		final var result = emailRepository.findByMunicipalityIdAndNamespace(municipalityId, namespace);
		return toEmails(result);
	}

	public void deleteEmail(final String id) {
		emailRepository.deleteById(id);
	}

	public List<CredentialsEntity> getAllCredentials() {
		return credentialsRepository.findAll();
	}

	public List<Email> getAllEmailsInInbox(final CredentialsEntity credentials, final String emailAddress) {
		return ewsIntegration.pageThroughEntireInbox(
			credentials.getUsername(), encryptionUtility.decrypt(credentials.getPassword()),
			credentials.getDomain(), emailAddress);
	}

	public List<EmailEntity> getOldEmails() {
		return emailRepository.findAll().stream()
			.filter(email -> email.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(1)))
			.toList();
	}

	public void sendReport() {
		final var oldEmailIds = getOldEmails().stream()
			.map(EmailEntity::getId)
			.toList();
		if (!oldEmailIds.isEmpty()) {
			messagingIntegration.sendEmail(format(EMAIL_MESSAGE, oldEmailIds), EMAIL_SUBJECT);
		}
	}

	@Transactional
	public void saveAndMoveEmail(final Email email, final String emailAddress, final CredentialsEntity credential) throws Exception {
		emailRepository.save(EmailMapper.toEmailEntity(email, credential.getNamespace(), credential.getMunicipalityId(), credential.getMetadata()));
		ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.id()), emailAddress, credential.getDestinationFolder());
	}
}
