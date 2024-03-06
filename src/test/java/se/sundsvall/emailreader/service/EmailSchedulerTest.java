package se.sundsvall.emailreader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createEmail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailSchedulerTest {

	@Mock
	private EmailService emailService;

	@InjectMocks
	private EmailScheduler emailScheduler;

	@Test
	void checkForNewEmails() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailAddresses = "someEmailAddress";
		final var email = createEmail();

		when(emailService.getAllCredentials()).thenReturn(List.of(credential));
		when(emailService.getAllEmailsInInbox(credential, emailAddresses)).thenReturn(List.of(email));
		doNothing().when(emailService).saveAndMoveEmail(email, emailAddresses, credential);

		emailScheduler.checkForNewEmails();

		verify(emailService).getAllCredentials();
		verify(emailService).getAllEmailsInInbox(credential, emailAddresses);
		verify(emailService).saveAndMoveEmail(email, emailAddresses, credential);
		verifyNoMoreInteractions(emailService);
	}

	@Test
	void checkForNewEmails_noCredentials() throws Exception {
		when(emailService.getAllCredentials()).thenReturn(List.of());

		emailScheduler.checkForNewEmails();

		verify(emailService).getAllCredentials();
		verify(emailService, never()).getAllEmailsInInbox(any(), any());
		verify(emailService, never()).saveAndMoveEmail(any(), any(), any());
		verifyNoMoreInteractions(emailService);
	}

	@Test
	void checkForOldEmailsAndSendReport() {
		doNothing().when(emailService).sendReport();

		emailScheduler.checkForOldEmailsAndSendReport();

		verify(emailService).sendReport();
		verifyNoMoreInteractions(emailService);
	}
}
