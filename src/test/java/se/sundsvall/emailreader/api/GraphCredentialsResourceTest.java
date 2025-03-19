package se.sundsvall.emailreader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.GraphCredentials;
import se.sundsvall.emailreader.service.GraphCredentialsService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class GraphCredentialsResourceTest {

	private static final String PATH = "/{municipalityId}/credentials/graph";
	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private GraphCredentialsService graphCredentialsService;

	@Test
	void getAllByMunicipalityId() {
		// Arrange
		when(graphCredentialsService.getCredentialsByMunicipalityId("2281")).thenReturn(List.of(GraphCredentials.builder().build()));

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", "2281")))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(GraphCredentials.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull().hasSize(1);
	}

	@Test
	void getByMunicipalityIdAndId() {
		// Arrange
		final var municipalityId = "2281";
		final var someId = UUID.randomUUID().toString();
		when(graphCredentialsService.getCredentialsByMunicipalityIdAndId(municipalityId, someId)).thenReturn(GraphCredentials.builder().build());

		// Act
		final var response = webTestClient.get()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", municipalityId, "id", someId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(GraphCredentials.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
	}

	@Test
	void create() {
		// Arrange
		final var municipalityId = "2281";
		final var destinationFolder = "someDestinationFolder";
		final var clientId = "someClientId";
		final var clientSecret = "someClientSecret";
		final var tenantId = "someTenantId";
		final var namespace = "someNamespace";
		final var emailAddress = Collections.singletonList("someEmailAddress");
		final var metadata = Collections.singletonMap("someKey", "someValue");
		final var id = UUID.randomUUID().toString();
		final var credentials = GraphCredentials.builder()
			.withDestinationFolder(destinationFolder)
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withNamespace(namespace)
			.withEmailAddress(emailAddress)
			.withMetadata(metadata)
			.build();

		when(graphCredentialsService.create(municipalityId, credentials)).thenReturn(id);

		// Act & Assert
		webTestClient.post()
			.uri(builder -> builder.path(PATH).build(Map.of("municipalityId", municipalityId)))
			.bodyValue(credentials)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL)
			.expectHeader().location("/" + municipalityId + "/credentials/graph/" + id)
			.expectBody().isEmpty();
	}

	@Test
	void update() {
		// Arrange
		final var municipalityId = "2281";
		final var someId = UUID.randomUUID().toString();
		final var destinationFolder = "someDestinationFolder";
		final var clientId = "someClientId";
		final var clientSecret = "someClientSecret";
		final var tenantId = "someTenantId";
		final var namespace = "someNamespace";
		final var emailAddress = Collections.singletonList("someEmailAddress");
		final var metadata = Collections.singletonMap("someKey", "someValue");
		final var credentials = GraphCredentials.builder()
			.withDestinationFolder(destinationFolder)
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withNamespace(namespace)
			.withEmailAddress(emailAddress)
			.withMetadata(metadata)
			.build();

		// Act & Assert
		webTestClient.put()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", municipalityId, "id", someId)))
			.bodyValue(credentials)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
	}

	@Test
	void delete() {
		// Arrange
		final var municipalityId = "2281";
		final var someId = UUID.randomUUID().toString();

		// Act & Assert
		webTestClient.delete()
			.uri(builder -> builder.path(PATH + "/{id}").build(Map.of("municipalityId", municipalityId, "id", someId)))
			.exchange()
			.expectStatus().isNoContent();
	}
}
