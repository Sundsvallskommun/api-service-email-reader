package se.sundsvall.emailreader.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
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

		verify(emailService, times(1)).getAllCredentials();
		verify(emailService, times(1)).getAllEmailsInInbox(credential, emailAddresses);
		verify(emailService, times(1)).saveAndMoveEmail(email, emailAddresses, credential);
		verifyNoMoreInteractions(emailService);
	}

	@Test
	void checkForOldEmailsAndSendReport() {
		doNothing().when(emailService).sendReport();

		emailScheduler.checkForOldEmailsAndSendReport();

		verify(emailService, times(1)).sendReport();
		verifyNoMoreInteractions(emailService);
	}
}
