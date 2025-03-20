package se.sundsvall.emailreader.apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.emailreader.Application;

@WireMockAppTestSuite(files = "classpath:/GraphCredentialsIT/",
	classes = Application.class)
@Sql(scripts = {
	"/sql/truncate.sql",
	"/sql/init-db.sql"
})
class GraphCredentialsIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final String PATH = "/{municipalityId}/credentials/graph";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String CREDENTIALS_ID = "81471222-5798-11e9-ae24-57fa13b361e1";

	@Test
	void test1_fetchCredentials() {

		setupCall()
			.withServicePath(builder -> fromPath(PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_createCredentials() {

		final var location = setupCall()
			.withServicePath(builder -> fromPath(PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		setupCall()
			.withServicePath(location)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_updateCredentials() {

		setupCall()
			.withServicePath(builder -> fromPath(PATH + "/{credentialsId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "credentialsId", CREDENTIALS_ID)))
			.withHttpMethod(PUT)
			.withExpectedResponseStatus(NO_CONTENT)
			.withRequest(REQUEST_FILE)
			.sendRequest();

		setupCall()
			.withServicePath(builder -> fromPath(PATH + "/{credentialsId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "credentialsId", CREDENTIALS_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_deleteCredentials() {

		setupCall()
			.withServicePath(builder -> fromPath(PATH + "/{credentialsId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "credentialsId", CREDENTIALS_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(PATH + "/{credentialsId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "credentialsId", CREDENTIALS_ID)))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(builder -> fromPath(PATH + "/{credentialsId}")
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "credentialsId", CREDENTIALS_ID)))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.sendRequestAndVerifyResponse();

	}

}
