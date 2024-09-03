package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createEmail;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.utility.EncryptionException;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
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
	private EmailRepository mockEmailRepository;

	@Mock
	private CredentialsRepository mockCredentialsRepository;

	@Mock
	private EWSIntegration mockEwsIntegration;

	@Mock
	private MessagingIntegration mockMessagingIntegration;

	@Mock
	private EncryptionUtility mockEncryptionUtility;

	private EmailService emailService;

	@BeforeEach
	void init() {
		emailService = new EmailService(mockEmailRepository, mockCredentialsRepository, mockMessagingIntegration, mockEwsIntegration, mockEncryptionUtility);
	}

	@Test
	void getAllEmails() {
		when(mockEmailRepository.findByMunicipalityIdAndNamespace("someMunicipalityId", "someNamespace"))
			.thenReturn(List.of(createEmailEntity()));

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

		verify(mockEmailRepository).findByMunicipalityIdAndNamespace("someMunicipalityId", "someNamespace");
		verifyNoMoreInteractions(mockEmailRepository);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void deleteEmail() {
		emailService.deleteEmail("2281", "someId");

		verify(mockEmailRepository, times(1)).deleteByMunicipalityIdAndId("2281", "someId");
		verifyNoMoreInteractions(mockEmailRepository);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void getAllCredentials() {
		when(mockCredentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

		final var credentials = emailService.getAllCredentials();

		assertThat(credentials).hasSize(1);

		verify(mockCredentialsRepository).findAll();
		verifyNoMoreInteractions(mockCredentialsRepository);
		verifyNoInteractions(mockEmailRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void getAllEmailsInInbox() {
		final var credentials = createCredentialsEntity();
		final var emailAddress = "someEmailAddress";

		when(mockEncryptionUtility.decrypt("somePassword")).thenReturn("somePassword");
		when(mockEwsIntegration.pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress))
			.thenReturn(List.of(createEmail(null)));

		final var emails = emailService.getAllEmailsInInbox(credentials, emailAddress);

		assertThat(emails).hasSize(1);

		verify(mockEwsIntegration).pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress);
		verifyNoMoreInteractions(mockEwsIntegration);
		verifyNoInteractions(mockEmailRepository, mockCredentialsRepository, mockMessagingIntegration);
	}

	@Test
	void getALlEmailsInInbox_decryptionException() {
		final var credentials = createCredentialsEntity();
		final var emailAddress = "someEmailAddress";

		when(mockEncryptionUtility.decrypt("somePassword")).thenThrow(new EncryptionException("someMessage"));

		final var emails = emailService.getAllEmailsInInbox(credentials, emailAddress);

		assertThat(emails).isEmpty();

		verify(mockEncryptionUtility).decrypt("somePassword");
		verifyNoMoreInteractions(mockEncryptionUtility);
		verifyNoInteractions(mockEmailRepository, mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration);
	}

	@Test
	void getOldEmails() {
		final var emailEntity = createEmailEntity();
		emailEntity.setCreatedAt(emailEntity.getCreatedAt().minusDays(2));

		when(mockEmailRepository.findAll()).thenReturn(List.of(emailEntity, createEmailEntity(), createEmailEntity()));

		final var emails = emailService.getOldEmails();

		assertThat(emails).hasSize(1);

		verify(mockEmailRepository).findAll();
		verifyNoMoreInteractions(mockEmailRepository);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void sendReport() {
		final var spy = Mockito.spy(emailService);
		final var emailEntity = createEmailEntity();
		emailEntity.setId("Test!");
		when(spy.getOldEmails()).thenReturn(List.of(emailEntity));
		doNothing().when(mockMessagingIntegration).sendEmail(eq("someMunicipalityId"), any(), any());

		spy.sendReport();

		verify(spy).getOldEmails();
		verify(spy).sendReport();
		verify(mockEmailRepository).findAll();
		verify(mockMessagingIntegration).sendEmail(
			"someMunicipalityId",
			"EmailReader has detected unhandled emails with the following IDs: [Test!]",
			"[Warning] EmailReader has detected unhandled emails");
		verifyNoMoreInteractions(mockMessagingIntegration);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmail() throws Exception {
		emailService.saveAndMoveEmail(createEmail(null), "someEmail", createCredentialsEntity());

		verify(mockEmailRepository).save(any());
		verify(mockEwsIntegration).moveEmail(any(), any(), any());
		verifyNoMoreInteractions(mockEmailRepository, mockEwsIntegration);
		verifyNoInteractions(mockCredentialsRepository, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmailWithoutMockedDB() throws Exception {

		assertThat(emailRepository.findByMunicipalityIdAndNamespace("municipality_id-1", "namespace-1")).isEmpty();
		final var service = new EmailService(emailRepository, credentialsRepository, mockMessagingIntegration, mockEwsIntegration, mockEncryptionUtility);
		final var credentialsEntity = credentialsRepository.findAll().getFirst();

		service.saveAndMoveEmail(createEmail(null), "someEmail", credentialsEntity);

		assertThat(credentialsRepository.findAll().getFirst().getMetadata()).isNotEmpty();
		assertThat(emailRepository.findByMunicipalityIdAndNamespace("municipality_id-1", "namespace-1")).isNotEmpty();

		verify(mockEwsIntegration).moveEmail(any(), any(), any());
		verifyNoMoreInteractions(mockEwsIntegration);
		verifyNoInteractions(mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmailWithAutoReply() throws Exception {

		final var headers = Map.of(Header.AUTO_SUBMITTED, List.of("auto-replied"));
		final var email = createEmail(headers);
		emailService.saveAndMoveEmail(email, "someEmail", createCredentialsEntity());

		verify(mockEwsIntegration).deleteEmail(any());
		verifyNoMoreInteractions(mockEwsIntegration);
		verifyNoInteractions(mockEmailRepository,mockCredentialsRepository, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void hasTransactionalAnnotation() throws Exception {
		final var method = EmailService.class.getDeclaredMethod("saveAndMoveEmail", Email.class, String.class, CredentialsEntity.class);
		assertThat(method.getAnnotation(Transactional.class)).isNotNull();
	}

}
