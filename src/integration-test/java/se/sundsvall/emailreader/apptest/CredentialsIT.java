package se.sundsvall.emailreader.apptest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/CredentialsIT/", classes = Application.class)
public class CredentialsIT extends AbstractAppTest {

	private static final String MARIADB_VERSION = "mariadb:10.6.12";

	@Container
	public static final MariaDBContainer<?> emaildb = new MariaDBContainer<>(DockerImageName.parse(MARIADB_VERSION))
		.withDatabaseName("emailreader")
		.withUsername("root")
		.withPassword("")
		.withInitScript("sql/init-db.sql");

	@Autowired
	CredentialsRepository credentialsRepository;

	/**
	 * get the url, user and password from the container and set them in the context.
	 */
	@DynamicPropertySource
	static void registerProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", emaildb::getJdbcUrl);
		registry.add("spring.datasource.username", emaildb::getUsername);
		registry.add("spring.datasource.password", emaildb::getPassword);
	}


	@Test
	void test1_fetchCredentials() throws Exception {

		final var response = setupCall()
			.withServicePath("/credentials?municipalityId=2281&namespace=someNamespace")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<List<Credentials>>() {

			});

		assertThat(response).hasSize(1).element(0).satisfies(
			credentials -> {
				assertThat(credentials.municipalityId()).isEqualTo("2281");
				assertThat(credentials.namespace()).isEqualTo("someNamespace");
				assertThat(credentials.username()).isEqualTo("someUsername");
				assertThat(credentials.emailAdress()).hasSize(2)
					.element(0).satisfies(emailAdress -> assertThat(emailAdress)
						.isEqualTo("inbox1@sundsvall.se"));
				assertThat(credentials.password()).isNull();
				assertThat(credentials.destinationFolder()).isEqualTo("someDestinationFolder");
				assertThat(credentials.id()).isNotNull();
				assertThat(credentials.domain()).isEqualTo("someDomain");

			});
	}

	@Test
	void test2_createCredentials() {

		setupCall()
			.withServicePath("/credentials")
			.withHttpMethod(POST)
			.withRequest("""
				{
				  "username": "joe01doe",
				  "password": "someSecretPassword",
				        "emailAdress": [
				          "myothersupportemail@sundsvall.se",
				          "mysupportemail@sundsvall.se"
				        ],
				  "domain": "https://mail.example.com/EWS/Exchange.asmx",
				  "municipalityId": "2281",
				  "namespace": "created.namespace",
				  "destinationFolder": "createdFolder"
				}"""
			)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.withExpectedResponseBodyIsNull();

		final var result = credentialsRepository.findAll().stream()
			.filter(credentials -> credentials.getNamespace().equals("created.namespace"))
			.findFirst()
			.orElseThrow();

		assertThat(result.getUsername()).isEqualTo("joe01doe");
		assertThat(result.getPassword()).isNotBlank().isNotEqualTo("someSecretPassword");
		assertThat(result.getDomain()).isEqualTo("https://mail.example.com/EWS/Exchange.asmx");
		assertThat(result.getMunicipalityId()).isEqualTo("2281");
		assertThat(result.getNamespace()).isEqualTo("created.namespace");
		assertThat(result.getDestinationFolder()).isEqualTo("createdFolder");
		assertThat(result.getId()).isNotNull();

		// cleanup
		credentialsRepository.delete(result);
	}

	@Test
	void test3_updateCredentials() {

		setupCall()
			.withServicePath("/credentials/81471222-5798-11e9-ae24-57fa13b361e1")
			.withHttpMethod(PUT)
			.withRequest("""
				{
				  "username": "joe02doe",
				  "password": "mySecretPassword",
				  "domain": "https://mail.example.com/EWS/Exchange.asmx",
				      "emailAdress": [
				        "myotherupdatedsupportemail@sundsvall.se",
				        "myupdatedsupportemail@sundsvall.se"
				      ],
				  "municipalityId": "2281",
				  "namespace": "updated.namespace",
				  "destinationFolder": "updatedFolder"
				}"""
			)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.withExpectedResponseBodyIsNull();


		final var result = credentialsRepository.findAll().stream()
			.filter(credentials -> credentials.getNamespace().equals("updated.namespace"))
			.findFirst()
			.orElseThrow();


		assertThat(result.getUsername()).isEqualTo("joe02doe");
		assertThat(result.getPassword()).isNotBlank().isNotEqualTo("mySecretPassword");
		assertThat(result.getDomain()).isEqualTo("https://mail.example.com/EWS/Exchange.asmx");
		assertThat(result.getMunicipalityId()).isEqualTo("2281");
		assertThat(result.getNamespace()).isEqualTo("updated.namespace");
		assertThat(result.getDestinationFolder()).isEqualTo("updatedFolder");
		assertThat(result.getId()).isNotNull();
		assertThat(result.getEmailAdress()).isNotNull().hasSize(2).element(0).satisfies(emailAdress -> assertThat(emailAdress)
			.isEqualTo("myotherupdatedsupportemail@sundsvall.se"));

	}


	@Test
	void test4_deleteCredentials() {

		setupCall()
			.withServicePath("/credentials/81471222-5798-11e9-ae24-57fa13b361e1")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse()
			.withExpectedResponseBodyIsNull();

		final var result = credentialsRepository.findAll();

		assertThat(result).isEmpty();

	}

}
