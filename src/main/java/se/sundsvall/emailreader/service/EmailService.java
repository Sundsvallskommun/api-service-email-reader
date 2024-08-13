package se.sundsvall.emailreader.service;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static se.sundsvall.emailreader.service.mapper.EmailMapper.toEmails;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.mapper.EmailMapper;
import se.sundsvall.emailreader.utility.EncryptionException;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import microsoft.exchange.webservices.data.property.complex.ItemId;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

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

	@Transactional
	public void deleteEmail(final String municipalityId, final String id) {
		emailRepository.deleteByMunicipalityIdAndId(municipalityId, id);
	}

	public List<CredentialsEntity> getAllCredentials() {
		return credentialsRepository.findAll();
	}

	public List<Email> getAllEmailsInInbox(final CredentialsEntity credential, final String emailAddress) {
		try {
			final var decryptedPassword = encryptionUtility.decrypt(credential.getPassword());
			return ewsIntegration.pageThroughEntireInbox(
				credential.getUsername(), decryptedPassword,
				credential.getDomain(), emailAddress);
		} catch (final EncryptionException e) {
			log.error("Failed to decrypt password for credential with id: {}", credential.getId(), e);
		}
		return emptyList();
	}

	public List<EmailEntity> getOldEmails() {
		return emailRepository.findAll().stream()
			.filter(email -> email.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(1)))
			.toList();
	}

	public void sendReport() {
		final var oldEmails = getOldEmails();

		if (!oldEmails.isEmpty()) {
			final var municipalityIds = oldEmails.stream()
				.map(EmailEntity::getMunicipalityId)
				.distinct()
				.toList();

			municipalityIds.forEach(municipalityId -> {
				final var oldEmailIds = oldEmails.stream()
					.filter(email -> email.getMunicipalityId().equals(municipalityId))
					.map(EmailEntity::getId)
					.toList();
				if (!oldEmailIds.isEmpty()) {
					messagingIntegration.sendEmail(municipalityId, format(EMAIL_MESSAGE, oldEmailIds), EMAIL_SUBJECT);
				}
			});
		}
	}

	@Transactional
	public void saveAndMoveEmail(final Email email, final String emailAddress, final CredentialsEntity credential) throws Exception {
		emailRepository.save(EmailMapper.toEmailEntity(email, credential.getNamespace(), credential.getMunicipalityId(), new HashMap<>(credential.getMetadata())));
		ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.id()), emailAddress, credential.getDestinationFolder());
	}

}
