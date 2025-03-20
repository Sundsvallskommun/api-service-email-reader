package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.emailreader.api.model.GraphCredentials;
import se.sundsvall.emailreader.integration.db.GraphCredentialsRepository;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;
import se.sundsvall.emailreader.utility.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
class GraphCredentialsServiceTest {

	@Mock
	private GraphCredentialsRepository graphCredentialsRepository;

	@Mock
	private EncryptionUtility encryptionUtility;

	@InjectMocks
	private GraphCredentialsService service;

	@Test
	void getCredentialsByMunicipalityId() {

		// Arrange
		final var entity = createEntity();
		final var municipalityId = "2281";
		when(graphCredentialsRepository.findAllByMunicipalityId(municipalityId)).thenReturn(List.of(entity));

		// Act
		final List<GraphCredentials> result = service.getCredentialsByMunicipalityId(municipalityId);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		verify(graphCredentialsRepository).findAllByMunicipalityId(municipalityId);
	}

	@Test
	void getCredentialByMunicipalityIdAndId() {
		// Arrange
		final var entity = createEntity();
		final var municipalityId = "2281";
		final var id = "someId";
		when(graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(entity));

		// Act
		final var result = service.getCredentialsByMunicipalityIdAndId(municipalityId, id);

		// Assert
		assertThat(result).isNotNull();
		verify(graphCredentialsRepository).findByMunicipalityIdAndId(municipalityId, id);
		verifyNoMoreInteractions(graphCredentialsRepository);
	}

	@Test
	void create() {
		// Arrange
		final var credentials = GraphCredentials.builder()
			.withClientId("clientId")
			.withClientSecret("clientSecret")
			.withTenantId("tenantId")
			.build();
		final var municipalityId = "2281";
		final var id = "someId";
		final var entity = createEntity();

		when(encryptionUtility.encrypt(any(byte[].class))).thenReturn("encryptedValue");
		when(graphCredentialsRepository.save(any(GraphCredentialsEntity.class))).thenReturn(entity);

		// Act
		final String result = service.create(municipalityId, credentials);

		// Assert
		assertThat(result).isEqualTo(id);
		verify(graphCredentialsRepository).save(any(GraphCredentialsEntity.class));
		verify(encryptionUtility, times(3)).encrypt(any(byte[].class));
		verifyNoMoreInteractions(graphCredentialsRepository, encryptionUtility);
	}

	@Test
	void update() {
		// Arrange
		final var entity = createEntity();
		final var credentials = createCredentials();
		final var id = "someId";
		final var municipalityId = "2281";
		when(graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(entity));
		when(encryptionUtility.encrypt(any(byte[].class))).thenReturn("encryptedValue");

		// Act
		service.update(municipalityId, id, credentials);

		// Assert
		verify(graphCredentialsRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(graphCredentialsRepository).save(any(GraphCredentialsEntity.class));
		verify(encryptionUtility, times(3)).encrypt(any(byte[].class));
		verifyNoMoreInteractions(graphCredentialsRepository, encryptionUtility);

	}

	@Test
	void updateEntityNotFound() {
		// Arrange
		final var credentials = createCredentials();
		final var municipalityId = "2281";
		final var id = "someId";

		when(graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> service.update(municipalityId, id, credentials))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Entity not found");

		verify(graphCredentialsRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(graphCredentialsRepository, never()).save(any(GraphCredentialsEntity.class));
		verifyNoInteractions(encryptionUtility);
		verifyNoMoreInteractions(graphCredentialsRepository);
	}

	@Test
	void delete() {
		// Arrange
		final var entity = createEntity();
		final var municipalityId = "2281";
		final var id = "someId";
		when(graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.of(entity));

		// Act
		service.delete(municipalityId, id);

		// Assert
		verify(graphCredentialsRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(graphCredentialsRepository).delete(entity);
		verifyNoInteractions(encryptionUtility);
		verifyNoMoreInteractions(graphCredentialsRepository);
	}

	@Test
	void deleteEntityNotFound() {
		// Arrange
		final var municipalityId = "2281";
		final var id = "someId";
		when(graphCredentialsRepository.findByMunicipalityIdAndId(municipalityId, id)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> service.delete(municipalityId, id))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Entity not found");

		verify(graphCredentialsRepository).findByMunicipalityIdAndId(municipalityId, id);
		verify(graphCredentialsRepository, never()).delete(any(GraphCredentialsEntity.class));
		verifyNoInteractions(encryptionUtility);
		verifyNoMoreInteractions(graphCredentialsRepository);
	}

	private GraphCredentialsEntity createEntity() {

		return GraphCredentialsEntity.builder()
			.withId("someId")
			.withMunicipalityId("2281")
			.withClientId("encryptedClientId")
			.withClientSecret("encryptedClientSecret")
			.withTenantId("encryptedTenantId")
			.build();
	}

	private GraphCredentials createCredentials() {
		return GraphCredentials.builder()
			.withClientId("clientId")
			.withClientSecret("clientSecret")
			.withTenantId("tenantId")
			.build();

	}
}
