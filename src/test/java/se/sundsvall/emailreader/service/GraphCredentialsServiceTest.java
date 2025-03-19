package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.emailreader.api.model.GraphCredentials;

@ExtendWith(MockitoExtension.class)
class GraphCredentialsServiceTest {

	@InjectMocks
	private GraphCredentialsService service;

	@Test
	void getCredentialsByMunicipalityId() {
		// Arrange
		final var municipalityId = "2281";

		// Act
		final var result = service.getCredentialsByMunicipalityId(municipalityId);

		// Assert
		assertThat(result).isNotNull().isEmpty();
		// TODO: Add assertions when repository is implemented
	}

	@Test
	void create() {
		// Arrange
		final var municipalityId = "2281";
		final var credentials = GraphCredentials.builder().build();
		// Act
		final var result = service.create(municipalityId, credentials);

		// Assert
		assertThat(result).isNull();
		// TODO: Add assertions when repository is implemented

	}

	@Test
	void update() {
		// Arrange
		final var municipalityId = "2281";
		final var id = "someId";
		final var credentials = GraphCredentials.builder().build();

		// Act
		service.update(municipalityId, id, credentials);

		// Assert
		// TODO: Add assertions when repository is implemented

	}

	@Test
	void delete() {
		// Arrange
		final var municipalityId = "2281";
		final var id = "someId";

		// Act
		service.delete(municipalityId, id);

		// Assert
		// TODO: Add assertions when repository is implemented
	}
}
