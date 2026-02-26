package se.sundsvall.emailreader.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Blob;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;
import se.sundsvall.emailreader.TestUtility;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.AttachmentRepository;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.ews.EWSMapper;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.utility.EncryptionException;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class EmailServiceTest {

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private CredentialsRepository credentialsRepository;

	@Mock
	private EmailRepository emailRepositoryMock;

	@Mock
	private CredentialsRepository credentialsRepositoryMock;

	@Mock
	private EWSIntegration ewsIntegrationMock;

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Mock
	private EncryptionUtility mockEncryptionUtility;

	@Mock
	private AttachmentRepository mockAttachmentRepository;

	@Mock
	private AttachmentEntity messageAttachmentEntityMock;

	@Mock
	private HttpServletResponse servletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@Mock
	private EWSMapper ewsMapperMock;

	@Mock
	private Consumer<String> consumerMock;

	@Mock
	private Blob blobMock;

	private EmailService emailService;

	@BeforeEach
	void init() {
		emailService = new EmailService(emailRepositoryMock, credentialsRepositoryMock, messagingIntegrationMock, ewsIntegrationMock, mockEncryptionUtility, mockAttachmentRepository, ewsMapperMock);
	}

	@Test
	void getAllEmails() {
		when(emailRepositoryMock.findByMunicipalityIdAndNamespace("someMunicipalityId", "someNamespace"))
			.thenReturn(List.of(createEmailEntity(emptyMap())));

		final var emails = emailService.getAllEmails("someMunicipalityId", "someNamespace");

		assertThat(emails).hasSize(1);
		assertThat(emails.getFirst()).isNotNull().satisfies(email -> {
			assertThat(email.id()).isEqualTo("someId");
			assertThat(email.subject()).isEqualTo("someSubject");
			assertThat(email.sender()).isEqualTo("someSender");
			assertThat(email.message()).isEqualTo("someMessage");
			assertThat(email.metadata()).hasSize(1).containsEntry("someKey", "someValue");
			assertThat(email.recipients().getFirst()).isEqualTo("someRecipient");
			assertThat(email.headers()).hasSize(3)
				.containsEntry(Header.MESSAGE_ID, List.of("someValue"))
				.containsEntry(Header.REFERENCES, List.of("someReferenceValue"))
				.containsEntry(Header.IN_REPLY_TO, List.of("someReplyToValue"));
		});

		verify(emailRepositoryMock).findByMunicipalityIdAndNamespace("someMunicipalityId", "someNamespace");
		verifyNoMoreInteractions(emailRepositoryMock);
		verifyNoInteractions(credentialsRepositoryMock, ewsIntegrationMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void deleteEmail() {
		emailService.deleteEmail("2281", "someId");

		verify(emailRepositoryMock, times(1)).deleteByMunicipalityIdAndId("2281", "someId");
		verifyNoMoreInteractions(emailRepositoryMock);
		verifyNoInteractions(credentialsRepositoryMock, ewsIntegrationMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void getAllCredentials() {
		when(credentialsRepositoryMock.findAll()).thenReturn(List.of(createCredentialsEntity()));

		final var credentials = emailService.getAllCredentials();

		assertThat(credentials).hasSize(1);

		verify(credentialsRepositoryMock).findAll();
		verifyNoMoreInteractions(credentialsRepositoryMock);
		verifyNoInteractions(emailRepositoryMock, ewsIntegrationMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void findAllByActionAndActive() {
		when(credentialsRepositoryMock.findAllByActionAndEnabled(any(), eq(true))).thenReturn(List.of(createCredentialsEntity()));

		final var credentials = emailService.findAllByActionAndActive("someAction");

		assertThat(credentials).hasSize(1);
		verify(credentialsRepositoryMock).findAllByActionAndEnabled("someAction", true);
		verifyNoMoreInteractions(credentialsRepositoryMock);
	}

	@Test
	void getAllEmailsInInbox() {
		final var credentials = createCredentialsEntity();
		final var emailAddress = "someEmailAddress";
		final var emailMessage = mock(EmailMessage.class);

		when(mockEncryptionUtility.decrypt("somePassword")).thenReturn("somePassword");
		when(ewsIntegrationMock.pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress, consumerMock))
			.thenReturn(List.of(emailMessage));

		final var emails = emailService.getAllEmailsInInbox(credentials, emailAddress, consumerMock);

		assertThat(emails).hasSize(1);

		verify(ewsIntegrationMock).pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress, consumerMock);
		verifyNoMoreInteractions(ewsIntegrationMock);
		verifyNoInteractions(emailRepositoryMock, credentialsRepositoryMock, messagingIntegrationMock);
	}

	@Test
	void getALlEmailsInInbox_decryptionException() {
		final var credentials = createCredentialsEntity();
		final var emailAddress = "someEmailAddress";

		when(mockEncryptionUtility.decrypt("somePassword")).thenThrow(new EncryptionException("someMessage"));

		final var emails = emailService.getAllEmailsInInbox(credentials, emailAddress, consumerMock);

		assertThat(emails).isEmpty();

		verify(mockEncryptionUtility).decrypt("somePassword");
		verifyNoMoreInteractions(mockEncryptionUtility);
		verifyNoInteractions(emailRepositoryMock, credentialsRepositoryMock, ewsIntegrationMock, messagingIntegrationMock);
	}

	@Test
	void getOldEmails() {
		final var emailEntity = createEmailEntity(emptyMap());
		emailEntity.setCreatedAt(emailEntity.getCreatedAt().minusDays(2));

		when(emailRepositoryMock.findAll()).thenReturn(List.of(emailEntity, createEmailEntity(emptyMap()), createEmailEntity(emptyMap())));

		final var emails = emailService.getOldEmails();

		assertThat(emails).hasSize(1);

		verify(emailRepositoryMock).findAll();
		verifyNoMoreInteractions(emailRepositoryMock);
		verifyNoInteractions(credentialsRepositoryMock, ewsIntegrationMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void sendReport() {
		final var spy = Mockito.spy(emailService);
		final var emailEntity = createEmailEntity(emptyMap());
		emailEntity.setId("Test!");
		when(spy.getOldEmails()).thenReturn(List.of(emailEntity));
		doNothing().when(messagingIntegrationMock).sendEmail(eq("someMunicipalityId"), any(), any());

		spy.sendReport();

		verify(spy).getOldEmails();
		verify(spy).sendReport();
		verify(emailRepositoryMock).findAll();
		verify(messagingIntegrationMock).sendEmail(
			"someMunicipalityId",
			"EmailReader has detected unhandled emails with the following IDs: [Test!]",
			"[Warning] EmailReader has detected unhandled emails");
		verifyNoMoreInteractions(messagingIntegrationMock);
		verifyNoInteractions(credentialsRepositoryMock, ewsIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmail() throws Exception {
		final var emailMessageMock = mock(EmailMessage.class);
		final var emailEntity = TestUtility.createEmailEntity(emptyMap());
		final var credentials = createCredentialsEntity();

		when(ewsIntegrationMock.loadMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);
		when(ewsIntegrationMock.loadHTMLMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);
		when(ewsMapperMock.toEmail(any(), any(), any(), any()))
			.thenReturn(emailEntity);

		emailService.saveAndMoveEmail(emailMessageMock, "someEmail", credentials, consumerMock);

		verify(emailRepositoryMock).saveAndFlush(same(emailEntity));
		verify(ewsIntegrationMock).moveEmail(ItemId.getItemIdFromString("someOriginalId"), "someEmail", credentials.getDestinationFolder());
		verifyNoMoreInteractions(emailRepositoryMock, ewsIntegrationMock);
		verifyNoInteractions(credentialsRepositoryMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmailWithoutMockedDB() throws Exception {

		// Precondition
		assertThat(emailRepository.findByMunicipalityIdAndNamespace("municipality_id-1", "namespace-1")).isEmpty();

		// Arrange
		final var emailMessageMock = mock(EmailMessage.class);
		final var service = new EmailService(emailRepository, credentialsRepository, messagingIntegrationMock, ewsIntegrationMock, mockEncryptionUtility, mockAttachmentRepository, ewsMapperMock);
		final var credentialsEntity = credentialsRepository.findAll().getFirst();

		final var emailId = UUID.randomUUID().toString();
		final var email = TestUtility.createEmailEntity(emptyMap());
		email.setId(null);
		email.setOriginalId(emailId);
		email.getAttachments().forEach(attachmentEntity -> attachmentEntity.setId(null));
		email.setNamespace("namespace-1");
		email.setMunicipalityId("municipality_id-1");

		when(ewsMapperMock.toEmail(any(), any(), any(), any()))
			.thenReturn(email);

		when(ewsIntegrationMock.loadMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);

		when(ewsIntegrationMock.loadHTMLMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);

		when(emailMessageMock.getBody()).thenReturn(new MessageBody("SomeBody"));

		// Act
		service.saveAndMoveEmail(emailMessageMock, "someEmail", credentialsEntity, consumerMock);

		// Assert
		assertThat(credentialsRepository.findAll().getFirst().getMetadata()).isNotEmpty();
		assertThat(emailRepository.findByMunicipalityIdAndNamespace("municipality_id-1", "namespace-1")).isNotEmpty();

		verify(ewsIntegrationMock).moveEmail(ItemId.getItemIdFromString(emailId), "someEmail", "destination_folder-1");
		verifyNoMoreInteractions(ewsIntegrationMock);
		verifyNoInteractions(messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmailWithAutoReply() throws Exception {

		// Arrange
		final var emailMessageMock = mock(EmailMessage.class);
		final var service = new EmailService(emailRepository, credentialsRepository, messagingIntegrationMock, ewsIntegrationMock, mockEncryptionUtility, mockAttachmentRepository, ewsMapperMock);
		final var credentialsEntity = credentialsRepository.findAll().getFirst();

		final var headers = Map.of(Header.AUTO_SUBMITTED, List.of("auto-replied"));
		final var emailId = UUID.randomUUID().toString();

		final var email = TestUtility.createEmailEntity(headers);
		email.setId(null);
		email.setOriginalId(emailId);
		email.getAttachments().forEach(attachmentEntity -> attachmentEntity.setId(null));
		email.setNamespace("namespace-1");
		email.setMunicipalityId("municipality_id-1");

		when(ewsMapperMock.toEmail(any(), any(), any(), any()))
			.thenReturn(email);

		when(ewsIntegrationMock.loadMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);
		when(ewsIntegrationMock.loadHTMLMessage(emailMessageMock, consumerMock))
			.thenReturn(emailMessageMock);

		// Act
		service.saveAndMoveEmail(emailMessageMock, "someEmail", credentialsEntity, consumerMock);

		// Assert
		verify(ewsIntegrationMock).moveEmail(ItemId.getItemIdFromString(emailId), "someEmail", "destination_folder-1");
		verifyNoMoreInteractions(ewsIntegrationMock);
		verifyNoInteractions(emailRepositoryMock, credentialsRepositoryMock, messagingIntegrationMock, mockEncryptionUtility);
	}

	@Test
	void hasTransactionalAnnotation() throws Exception {
		final var method = EmailService.class.getDeclaredMethod("saveAndMoveEmail", EmailMessage.class, String.class, CredentialsEntity.class, Consumer.class);
		assertThat(method.getAnnotation(Transactional.class)).isNotNull();
	}

	@Test
	void getMessageAttachmentStreamed() throws Exception {
		final var attachmentId = 12L;
		final var content = "content";
		final var contentType = "contentType";
		final var fileName = "fileName";
		final var inputStream = IOUtils.toInputStream(content, UTF_8);
		when(mockAttachmentRepository.findById(any())).thenReturn(Optional.of(messageAttachmentEntityMock));
		when(messageAttachmentEntityMock.getContentType()).thenReturn(contentType);
		when(messageAttachmentEntityMock.getName()).thenReturn(fileName);
		when(messageAttachmentEntityMock.getContent()).thenReturn(blobMock);
		when(blobMock.length()).thenReturn((long) content.length());
		when(blobMock.getBinaryStream()).thenReturn(inputStream);
		when(servletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		emailService.getMessageAttachmentStreamed(attachmentId, servletResponseMock);

		verify(mockAttachmentRepository).findById(attachmentId);
		verify(messageAttachmentEntityMock).getContent();
		verify(servletResponseMock).addHeader(CONTENT_TYPE, contentType);
		verify(servletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
		verify(servletResponseMock).setContentLength(content.length());
		verify(servletResponseMock).getOutputStream();
	}

	@Test
	void getMessageAttachmentStreamedWhenAttachmentIsNotFound() {
		when(mockAttachmentRepository.findById(any())).thenReturn(Optional.empty());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> emailService.getMessageAttachmentStreamed(123, servletResponseMock))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(NOT_FOUND));

		verify(mockAttachmentRepository).findById(123L);
		verifyNoMoreInteractions(mockAttachmentRepository);
		verifyNoInteractions(messageAttachmentEntityMock, blobMock, servletOutputStreamMock);
	}

	@Test
	void getMessageAttachmentStreamedWhenExceptionIsThrown() {
		when(mockAttachmentRepository.findById(any())).thenReturn(Optional.of(messageAttachmentEntityMock));
		when(messageAttachmentEntityMock.getContent()).thenAnswer(t -> { throw new IOException(); });

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> emailService.getMessageAttachmentStreamed(123, servletResponseMock))
			.satisfies(problem -> assertThat(problem.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR));

		verify(mockAttachmentRepository).findById(123L);
		verify(messageAttachmentEntityMock).getContent();
		verifyNoMoreInteractions(mockAttachmentRepository, messageAttachmentEntityMock);
		verifyNoInteractions(blobMock, servletOutputStreamMock);
	}
}
