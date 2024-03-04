package se.sundsvall.emailreader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import microsoft.exchange.webservices.data.property.complex.ItemId;


@ExtendWith(MockitoExtension.class)
class EmailSchedulerTest {


	@Mock
	EncryptionUtility encryptionUtility;

	@Mock
	private MessagingIntegration messagingIntegration;

	@Mock
	private EWSIntegration ewsIntegration;

	@Mock
	private EmailRepository emailRepository;

	@Mock
	private CredentialsRepository credentialsRepository;

	@InjectMocks
	private EmailScheduler emailScheduler;

	@Test
	void checkForNewEmailsWithNoCredentials() {
		when(credentialsRepository.findAll()).thenReturn(List.of());

		emailScheduler.checkForNewEmails();

		verify(credentialsRepository, times(1)).findAll();
		verifyNoMoreInteractions(emailRepository, ewsIntegration, credentialsRepository, encryptionUtility);
		verifyNoInteractions(messagingIntegration);

	}

	@Test
	void checkForOldEmails() {
		final var email = createEmailEntity();
		email.setCreatedAt(email.getCreatedAt().minusDays(2));

		final var email2 = createEmailEntity();
		email2.setCreatedAt(email2.getCreatedAt().minusDays(1));
		email2.setId("someOtherId");
		when(emailRepository.findAll()).thenReturn(List.of(email, email2));

		emailScheduler.checkForOldEmails();

		verify(messagingIntegration, times(1)).sendEmail(any(String.class), any(String.class));
		verify(emailRepository, times(1)).findAll();
		verifyNoMoreInteractions(messagingIntegration, emailRepository);
		verifyNoInteractions(ewsIntegration, credentialsRepository, encryptionUtility);
	}

	@Test
	void handleNewEmails() throws Exception {
		when(emailRepository.save(any(EmailEntity.class))).thenReturn(createEmailEntity());

		emailScheduler.handleNewEmails(List.of(createEmail()), "someEmail", createCredentialsEntity());

		verify(emailRepository, times(1)).save(any(EmailEntity.class));
		verify(ewsIntegration, times(1))
			.moveEmail(any(ItemId.class), any(String.class), any(String.class));
		verifyNoMoreInteractions(emailRepository, ewsIntegration);
	}

	@Test
	void handleNewEmails_whenNotSaved(){
		doThrow(new RuntimeException("Database error")).when(emailRepository).save(any(EmailEntity.class));

		emailScheduler.handleNewEmails(List.of(createEmail()), "someEmail", createCredentialsEntity());

		verify(emailRepository, times(1)).save(any(EmailEntity.class));
		verify(emailRepository, times(1)).deleteById(any());
		verifyNoInteractions(ewsIntegration);
	}

	@Test
	void handleNewEmails_whenNotMoved() throws Exception {
		var emailEntity = createEmailEntity();

		when(emailRepository.save(any(EmailEntity.class))).thenReturn(emailEntity);
		doThrow(new Exception("Move error")).when(ewsIntegration).moveEmail(any(ItemId.class), any(String.class), any(String.class));

		emailScheduler.handleNewEmails(List.of(createEmail()), "someEmail", createCredentialsEntity());

		verify(emailRepository, times(1)).save(any(EmailEntity.class));
		verify(ewsIntegration, times(1))
			.moveEmail(any(ItemId.class), any(String.class), any(String.class));
		verify(emailRepository, times(1)).deleteById(emailEntity.getId());
		verifyNoMoreInteractions(emailRepository, ewsIntegration);
	}

	@Test
	void checkForNewEmails() throws Exception {
		var credentials = List.of(createCredentialsEntity());
		when(credentialsRepository.findAll()).thenReturn(credentials);
		when(ewsIntegration.pageThroughEntireInbox(any(), any(), any(), any())).thenReturn(List.of(createEmail()));
		when(emailRepository.save(any(EmailEntity.class))).thenReturn(createEmailEntity());

		emailScheduler.checkForNewEmails();

		verify(ewsIntegration, times(1)).pageThroughEntireInbox(any(), any(), any(), any());
		verify(ewsIntegration, times(1)).moveEmail(any(ItemId.class), any(String.class), any(String.class));
		verifyNoMoreInteractions(ewsIntegration, emailRepository);
	}
}
