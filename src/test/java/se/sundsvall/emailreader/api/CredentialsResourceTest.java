package se.sundsvall.emailreader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private CredentialsService service;

	@InjectMocks
	private CredentialsResource resource;

	@Test
	void getAll() {

		when(service.getCredentialsByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(createCredentials()));

		final var result = resource.getAllByMunicipalityId(MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		assertThat(result.getBody()).isNotNull().hasSize(1);
		assertThat(result.getBody()).element(0).satisfies(
			credentials -> {
				assertThat(credentials.id()).isEqualTo("someId");
				assertThat(credentials.destinationFolder()).isEqualTo("someDestinationFolder");
				assertThat(credentials.domain()).isEqualTo("someDomain");
				assertThat(credentials.namespace()).isEqualTo("someNamespace");
				assertThat(credentials.username()).isEqualTo("someUsername");
				assertThat(credentials.metadata()).hasSize(1).containsEntry("someKey", "someValue");
				assertThat(credentials.emailAddress()).hasSize(1).element(0).satisfies(emailAddress -> assertThat(emailAddress).isEqualTo("someEmailAddress"));
				assertThat(credentials.password()).isNull();
			});

		verify(service).getCredentialsByMunicipalityId(MUNICIPALITY_ID);
		verifyNoMoreInteractions(service);
	}

	@Test
	void create() {

		doNothing().when(service).create(eq(MUNICIPALITY_ID), any(Credentials.class));

		final var credentials = createCredentials();

		final var result = resource.create(MUNICIPALITY_ID, credentials);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		verify(service).create(eq(MUNICIPALITY_ID), any(Credentials.class));
		verifyNoMoreInteractions(service);
	}

	@Test
	void update() {

		doNothing().when(service).update(eq(MUNICIPALITY_ID), any(String.class), any(Credentials.class));

		final var credentials = createCredentials();

		final var result = resource.update(MUNICIPALITY_ID, "someId", credentials);

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		verify(service).update(eq(MUNICIPALITY_ID), any(String.class), any(Credentials.class));
		verifyNoMoreInteractions(service);
	}

	@Test
	void delete() {

		doNothing().when(service).delete(eq(MUNICIPALITY_ID), any(String.class));

		final var result = resource.delete(MUNICIPALITY_ID, "someId");

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		verify(service).delete(eq(MUNICIPALITY_ID), any(String.class));
		verifyNoMoreInteractions(service);
	}
}
