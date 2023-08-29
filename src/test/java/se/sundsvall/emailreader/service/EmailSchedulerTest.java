package se.sundsvall.emailreader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.utility.EncryptionUtility;


@ExtendWith(MockitoExtension.class)
class EmailSchedulerTest {

    @Mock
    EncryptionUtility encryptionUtility;

    @Mock
    private EWSIntegration ewsIntegration;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private CredentialsRepository credentialsRepository;

    @InjectMocks
    private EmailScheduler emailScheduler;


    @Test
    void scheduleEmailReader() {

        when(credentialsRepository.findAll()).thenReturn(List.of(createCredentialsEntity()));

        when(ewsIntegration.pageThroughEntireInbox(any(String.class),
            any(String.class), any(String.class), any(String.class)))
            .thenReturn(List.of(createEmail()));

        when(encryptionUtility.decrypt(any(String.class))).thenReturn("somePassword");

        emailScheduler.scheduleEmailReader();

        verify(ewsIntegration, times(1))
            .pageThroughEntireInbox(any(String.class),
                any(String.class), any(String.class), any(String.class));

        verify(emailRepository, times(1)).saveAll(anyList());
        verify(credentialsRepository, times(1)).findAll();
        verify(encryptionUtility, times(1)).decrypt(any(String.class));
        verifyNoMoreInteractions(emailRepository, ewsIntegration, credentialsRepository, encryptionUtility);

    }

}
