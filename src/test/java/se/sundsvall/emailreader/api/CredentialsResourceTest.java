package se.sundsvall.emailreader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentials;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.service.CredentialsService;

@ExtendWith(MockitoExtension.class)
class CredentialsResourceTest {

    @Mock
    private CredentialsService service;

    @InjectMocks
    private CredentialsResource resource;

    @Test
    void getAll() {

        when(service.getAllCredentials()).thenReturn(List.of(createCredentials()));

        final var result = resource.getAll();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody()).isNotNull().hasSize(1);
        assertThat(result.getBody()).element(0).satisfies(
            credentials -> {
                assertThat(credentials.id()).isEqualTo("someId");
                assertThat(credentials.destinationFolder()).isEqualTo("someDestinationFolder");
                assertThat(credentials.domain()).isEqualTo("someDomain");
                assertThat(credentials.namespace()).isEqualTo("someNamespace");
                assertThat(credentials.municipalityId()).isEqualTo("someMunicipalityId");
                assertThat(credentials.username()).isEqualTo("someUsername");
                assertThat(credentials.emailAdress()).hasSize(1).element(0).satisfies(emailAdress ->
                    assertThat(emailAdress).isEqualTo("someEmailAdress"));
                assertThat(credentials.password()).isNull();
            }
        );

        verify(service, times(1)).getAllCredentials();
        verifyNoMoreInteractions(service);
    }

    @Test
    void create() {

        doNothing().when(service).create(any(Credentials.class));

        final var credentials = createCredentials();

        final var result = resource.create(credentials);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        verify(service, times(1)).create(any(Credentials.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void update() {

        doNothing().when(service).update(any(String.class), any(Credentials.class));

        final var credentials = createCredentials();

        final var result = resource.update("someId", credentials);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        verify(service, times(1)).update(any(String.class), any(Credentials.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    void delete() {

        doNothing().when(service).delete(any(String.class));

        final var result = resource.delete("someId");

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        verify(service, times(1)).delete(any(String.class));
        verifyNoMoreInteractions(service);
    }


}
