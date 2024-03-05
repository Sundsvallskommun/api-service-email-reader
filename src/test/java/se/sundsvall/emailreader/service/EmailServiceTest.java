package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createEmail;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

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

	@InjectMocks
	private EmailService emailService;

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
		emailService.deleteEmail("someId");

		verify(mockEmailRepository, times(1)).deleteById("someId");
		verifyNoMoreInteractions(mockEmailRepository);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void getAllCredentials() {
		when(mockCredentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

		var credentials = emailService.getAllCredentials();

		assertThat(credentials).hasSize(1);

		verify(mockCredentialsRepository).findAll();
		verifyNoMoreInteractions(mockCredentialsRepository);
		verifyNoInteractions(mockEmailRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void getAllEmailsInInbox() {
		var credentials = createCredentialsEntity();
		var emailAddress = "someEmailAddress";

		when(mockEncryptionUtility.decrypt("somePassword")).thenReturn("somePassword");
		when(mockEwsIntegration.pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress))
			.thenReturn(List.of(createEmail()));

		var emails = emailService.getAllEmailsInInbox(credentials, emailAddress);

		assertThat(emails).hasSize(1);

		verify(mockEwsIntegration).pageThroughEntireInbox(credentials.getUsername(), "somePassword", credentials.getDomain(), emailAddress);
		verifyNoMoreInteractions(mockEwsIntegration);
		verifyNoInteractions(mockEmailRepository, mockCredentialsRepository, mockMessagingIntegration);
	}

	@Test
	void getOldEmails() {
		var emailEntity = createEmailEntity();
		emailEntity.setCreatedAt(emailEntity.getCreatedAt().minusDays(2));

		when(mockEmailRepository.findAll()).thenReturn(List.of(emailEntity, createEmailEntity(), createEmailEntity()));

		var emails = emailService.getOldEmails();

		assertThat(emails).hasSize(1);

		verify(mockEmailRepository).findAll();
		verifyNoMoreInteractions(mockEmailRepository);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockMessagingIntegration, mockEncryptionUtility);
	}

	@Test
	void sendReport() {
		var spy = Mockito.spy(emailService);
		var emailEntity = createEmailEntity();
		emailEntity.setId("Test!");
		when(spy.getOldEmails()).thenReturn(List.of(emailEntity));
		doNothing().when(mockMessagingIntegration).sendEmail(any(), any());

		spy.sendReport();

		verify(spy).getOldEmails();
		verify(spy).sendReport();
		verify(mockEmailRepository).findAll();
		verify(mockMessagingIntegration).sendEmail(
			"EmailReader has detected unhandled emails with the following IDs: [Test!]",
			"[Warning] EmailReader has detected unhandled emails");
		verifyNoMoreInteractions(mockMessagingIntegration);
		verifyNoInteractions(mockCredentialsRepository, mockEwsIntegration, mockEncryptionUtility);
	}

	@Test
	void saveAndMoveEmail() throws Exception {
		emailService.saveAndMoveEmail(createEmail(), "someEmail", createCredentialsEntity());

		verify(mockEmailRepository).save(any());
		verify(mockEwsIntegration).moveEmail(any(), any(), any());
		verifyNoMoreInteractions(mockEmailRepository, mockEwsIntegration);
		verifyNoInteractions(mockCredentialsRepository, mockMessagingIntegration, mockEncryptionUtility);
	}
}
