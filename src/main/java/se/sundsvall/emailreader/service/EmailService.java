package se.sundsvall.emailreader.service;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;
import static se.sundsvall.emailreader.api.model.Header.AUTO_SUBMITTED;
import static se.sundsvall.emailreader.service.mapper.EmailMapper.toEmails;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.AttachmentRepository;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.ews.EWSMapper;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.utility.EncryptionException;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@Service
public class EmailService {

	private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

	private static final String EMAIL_SUBJECT = "[Warning] EmailReader has detected unhandled emails";
	private static final String EMAIL_MESSAGE = "EmailReader has detected unhandled emails with the following IDs: {0}";

	private final EmailRepository emailRepository;
	private final CredentialsRepository credentialsRepository;

	private final MessagingIntegration messagingIntegration;
	private final EWSIntegration ewsIntegration;

	private final EncryptionUtility encryptionUtility;

	private final AttachmentRepository attachmentRepository;

	private final EWSMapper ewsMapper;

	public EmailService(final EmailRepository emailRepository,
		final CredentialsRepository credentialsRepository,
		final MessagingIntegration messagingIntegration,
		final EWSIntegration ewsIntegration,
		final EncryptionUtility encryptionUtility, final AttachmentRepository attachmentRepository, final EWSMapper ewsMapper) {
		this.emailRepository = emailRepository;
		this.credentialsRepository = credentialsRepository;
		this.messagingIntegration = messagingIntegration;
		this.ewsIntegration = ewsIntegration;
		this.encryptionUtility = encryptionUtility;
		this.attachmentRepository = attachmentRepository;
		this.ewsMapper = ewsMapper;
	}

	public List<Email> getAllEmails(final String municipalityId, final String namespace) {
		final var result = emailRepository.findByMunicipalityIdAndNamespace(municipalityId, namespace);
		return toEmails(result);
	}

	@Transactional
	public void deleteEmail(final String municipalityId, final String id) {
		final var sanitizedId = sanitizeForLogging(id);
		LOG.info("Deleting email with id '{}'", sanitizedId);
		emailRepository.deleteByMunicipalityIdAndId(municipalityId, id);
	}

	public List<CredentialsEntity> getAllCredentials() {
		return credentialsRepository.findAll();
	}

	public List<CredentialsEntity> findAllByActionAndActive(final String action) {
		return credentialsRepository.findAllByActionAndEnabled(action, true);
	}

	public List<EmailMessage> getAllEmailsInInbox(final CredentialsEntity credential, final String emailAddress, final Consumer<String> setUnHealthyConsumer) {
		try {
			final var decryptedPassword = encryptionUtility.decrypt(credential.getPassword());
			return ewsIntegration.pageThroughEntireInbox(
				credential.getUsername(), decryptedPassword,
				credential.getDomain(), emailAddress,
				setUnHealthyConsumer);
		} catch (final EncryptionException e) {
			LOG.error("Failed to decrypt password for credential with id: {}", credential.getId(), e);
			setUnHealthyConsumer.accept("[EWS] Failed to decrypt password for credential");
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
	public void saveAndMoveEmail(final EmailMessage ewsEmail, final String emailAddress, final CredentialsEntity credential, final Consumer<String> setUnHealthyConsumer) throws Exception {

		final var email = ewsMapper.toEmail(ewsIntegration.loadMessage(ewsEmail, setUnHealthyConsumer), credential.getMunicipalityId(), credential.getNamespace(), credential.getMetadata());

		if (email == null) {
			LOG.warn("[{}]: Email could not be mapped from EWS message, skipping email with id: {}, ", credential.getEmailAddress(), ewsEmail.getId().getUniqueId());
			return;
		}

		if (isAutoReply(email)) {
			LOG.info("[{}]: Email with original id '{}' has been interpreted as an auto-reply and will be moved to {} without further processing.", credential.getEmailAddress(), email.getOriginalId(), credential.getDestinationFolder());
			ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.getOriginalId()), emailAddress, credential.getDestinationFolder());
			return;
		}

		LOG.info("[{}]: Saving ews email with original id '{}'", credential.getEmailAddress(), email.getOriginalId());
		emailRepository.saveAndFlush(email);
		LOG.info("[{}]: Moving ews email with original id '{}' to folder '{}'", credential.getEmailAddress(), email.getOriginalId(), credential.getDestinationFolder());
		ewsIntegration.moveEmail(ItemId.getItemIdFromString(email.getOriginalId()), emailAddress, credential.getDestinationFolder());
	}

	boolean isAutoReply(final EmailEntity email) {
		return email.getHeaders().stream()
			.filter(header -> AUTO_SUBMITTED.getName().equalsIgnoreCase(header.getHeader().getName()))
			.flatMap(header -> header.getValues().stream())
			.anyMatch(value -> !"No".equalsIgnoreCase(value));
	}

	public void getMessageAttachmentStreamed(final long attachmentId, final HttpServletResponse response) {

		try {
			final var attachmentEntity = attachmentRepository
				.findById(attachmentId)
				.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "MessageAttachment not found"));

			final var file = attachmentEntity.getContent();

			response.addHeader(CONTENT_TYPE, attachmentEntity.getContentType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength((int) file.length());
			StreamUtils.copy(file.getBinaryStream(), response.getOutputStream());
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}

	@Transactional
	public void saveEmail(final EmailEntity email) {
		if (isAutoReply(email)) {
			return;
		}
		LOG.info("Saving graph email with original id '{}'", email.getOriginalId());
		emailRepository.save(email);

	}
}
