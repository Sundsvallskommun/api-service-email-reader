package se.sundsvall.emailreader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
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
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
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
    void checkForNewEmails() throws Exception {

        when(credentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

        when(ewsIntegration.pageThroughEntireInbox(any(String.class),
            any(String.class), any(String.class), any(String.class)))
            .thenReturn(List.of(createEmail()));

        when(encryptionUtility.decrypt(any(String.class))).thenReturn("somePassword");

        emailScheduler.checkForNewEmails();

        verify(ewsIntegration, times(1))
            .pageThroughEntireInbox(
                any(String.class), any(String.class), any(String.class), any(String.class));

        verify(ewsIntegration, times(1))
            .moveEmail(any(ItemId.class), any(String.class), any(String.class));

        verify(emailRepository, times(1)).save(any(EmailEntity.class));
        verify(credentialsRepository, times(1)).findAll();
        verify(encryptionUtility, times(1)).decrypt(any(String.class));
        verifyNoMoreInteractions(emailRepository, ewsIntegration, credentialsRepository, encryptionUtility);
        verifyNoInteractions(messagingIntegration);

    }

    @Test
    void checkForNewEmailsWithNoCredentials() {

        when(credentialsRepository.findAll()).thenReturn(List.of());

        emailScheduler.checkForNewEmails();

        verify(credentialsRepository, times(1)).findAll();
        verifyNoMoreInteractions(emailRepository, ewsIntegration, credentialsRepository, encryptionUtility);
        verifyNoInteractions(messagingIntegration);

    }

    @Test
    void checkForNewEmailsWithDatabaseFault() {

        given(emailRepository.save(any(EmailEntity.class))).willThrow(new RuntimeException("Database error"));

        when(credentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

        when(ewsIntegration.pageThroughEntireInbox(any(String.class),
            any(String.class), any(String.class), any(String.class)))
            .thenReturn(List.of(createEmail()));

        when(encryptionUtility.decrypt(any(String.class))).thenReturn("somePassword");

        emailScheduler.checkForNewEmails();

        verify(ewsIntegration, times(1))
            .pageThroughEntireInbox(
                any(String.class), any(String.class), any(String.class), any(String.class));

        verify(emailRepository, times(1)).save(any(EmailEntity.class));
        verify(credentialsRepository, times(1)).findAll();
        verify(encryptionUtility, times(1)).decrypt(any(String.class));
        verifyNoMoreInteractions(emailRepository, ewsIntegration, credentialsRepository, encryptionUtility);
        verifyNoInteractions(messagingIntegration);
    }

    @Test
    void checkForNewEmailsWithMoveFault() throws Exception {

        willAnswer(invocation -> {
            throw new RuntimeException("Move error");
        }).given(ewsIntegration).moveEmail(any(ItemId.class), any(String.class), any(String.class));

        when(credentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

        when(ewsIntegration.pageThroughEntireInbox(any(String.class),
            any(String.class), any(String.class), any(String.class)))
            .thenReturn(List.of(createEmail()));

        when(encryptionUtility.decrypt(any(String.class))).thenReturn("somePassword");

        emailScheduler.checkForNewEmails();

        verify(ewsIntegration, times(1))
            .pageThroughEntireInbox(
                any(String.class), any(String.class), any(String.class), any(String.class));

        verify(ewsIntegration, times(1))
            .moveEmail(any(ItemId.class), any(String.class), any(String.class));

        verify(emailRepository, times(1)).save(any(EmailEntity.class));
        verify(credentialsRepository, times(1)).findAll();
        verify(encryptionUtility, times(1)).decrypt(any(String.class));
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

}
