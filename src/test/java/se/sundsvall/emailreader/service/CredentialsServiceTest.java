package se.sundsvall.emailreader.service;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;
import se.sundsvall.emailreader.utility.EncryptionUtility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createCredentialsWithPassword;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private EncryptionUtility encryptionUtility;

	@Mock
	private CredentialsRepository repository;

	@InjectMocks
	private CredentialsService service;

	@Test
	void getCredentialsByMunicipalityId() {

		when(repository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(createCredentialsEntity()));

		final var result = service.getCredentialsByMunicipalityId(MUNICIPALITY_ID);

		assertThat(result).isNotNull().isNotEmpty().element(0).satisfies(
			credentials -> {
				assertThat(credentials.id()).isEqualTo("someId");
				assertThat(credentials.destinationFolder()).isEqualTo("someDestinationFolder");
				assertThat(credentials.domain()).isEqualTo("someDomain");
				assertThat(credentials.namespace()).isEqualTo("someNamespace");
				assertThat(credentials.username()).isEqualTo("someUsername");
				assertThat(credentials.metadata()).hasSize(1).containsEntry("someKey", "someValue");
				assertThat(credentials.password()).isNull();
			});

	}

	@Test
	void create() {

		when(encryptionUtility.encrypt(any(byte[].class))).thenReturn("someEncryptedPassword");

		final var result = createCredentialsWithPassword("somePassword");
		service.create(MUNICIPALITY_ID, result);

		verify(encryptionUtility).encrypt(any(byte[].class));
		verify(repository).save(any(CredentialsEntity.class));
		verifyNoMoreInteractions(repository, encryptionUtility);

	}

	@Test
	void delete() {

		service.delete(MUNICIPALITY_ID, "someId");

		verify(repository).deleteByMunicipalityIdAndId(eq(MUNICIPALITY_ID), any());
		verifyNoMoreInteractions(repository);
		verifyNoInteractions(encryptionUtility);
	}

	@Test
	void update() {

		when(repository.findByMunicipalityIdAndId(eq(MUNICIPALITY_ID), any())).thenReturn(java.util.Optional.of(createCredentialsEntity()));

		service.update(MUNICIPALITY_ID, "someId", createCredentialsWithPassword("somePassword"));

		verify(repository).findByMunicipalityIdAndId(eq(MUNICIPALITY_ID), any());
		verify(repository).save(any());
		verify(encryptionUtility).encrypt(any());
		verifyNoMoreInteractions(repository, encryptionUtility);
	}
}
