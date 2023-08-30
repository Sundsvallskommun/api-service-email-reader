package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createCredentialsWithPassword;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.utility.EncryptionUtility;


@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

    @Mock
    EncryptionUtility encryptionUtility;

    @Mock
    CredentialsRepository repository;

    @InjectMocks
    private CredentialsService service;

    @Test
    void getAllCredentials() {

        when(repository.findAll()).thenReturn(List.of(createCredentialsEntity()));

        final var result = service.getAllCredentials();

        assertThat(result).isNotNull().isNotEmpty().element(0).satisfies(
            credentials -> {
                assertThat(credentials.id()).isEqualTo("someId");
                assertThat(credentials.destinationFolder()).isEqualTo("someDestinationFolder");
                assertThat(credentials.domain()).isEqualTo("someDomain");
                assertThat(credentials.namespace()).isEqualTo("someNamespace");
                assertThat(credentials.municipalityId()).isEqualTo("someMunicipalityId");
                assertThat(credentials.username()).isEqualTo("someUsername");
                assertThat(credentials.password()).isNull();
            }
        );

    }

    @Test
    void create() {

        when(encryptionUtility.encrypt(any(byte[].class))).thenReturn("someEncryptedPassword");

        final var result = createCredentialsWithPassword("somePassword");
        service.create(result);

        verify(encryptionUtility).encrypt(any(byte[].class));
        verify(repository).save(any(CredentialsEntity.class));
        verifyNoMoreInteractions(repository, encryptionUtility);

    }

    @Test
    void delete() {

        service.delete("someId");

        verify(repository, times(1)).deleteById(any());
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(encryptionUtility);
    }

    @Test
    void update() {

        when(repository.findById(any())).thenReturn(java.util.Optional.of(createCredentialsEntity()));

        service.update("someId", createCredentialsWithPassword("somePassword"));

        verify(repository, times(1)).findById(any());
        verify(repository, times(1)).save(any());
        verify(encryptionUtility, times(1)).encrypt(any());
        verifyNoMoreInteractions(repository, encryptionUtility);
    }

}
