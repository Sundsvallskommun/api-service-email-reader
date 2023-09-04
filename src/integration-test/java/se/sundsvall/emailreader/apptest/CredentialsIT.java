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
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.Credentials;
import se.sundsvall.emailreader.integration.db.CredentialsRepository;

@WireMockAppTestSuite(files = "classpath:/CredentialsIT/",
	classes = Application.class)
@Sql(scripts = {
	"/sql/truncate.sql",
	"/sql/init-db.sql"
})
class CredentialsIT extends AbstractAppTest {

	@Autowired
	CredentialsRepository credentialsRepository;

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
				assertThat(credentials.emailAddress()).hasSize(2)
					.element(0).satisfies(emailAddress -> assertThat(emailAddress)
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
				        "emailAddress": [
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
				      "emailAddress": [
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
		assertThat(result.getEmailAddress()).isNotNull().hasSize(2).element(0).satisfies(emailAddress -> assertThat(emailAddress)
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
