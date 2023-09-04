package se.sundsvall.emailreader.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
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
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.EmailRepository;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EmailIT/", classes = Application.class)
public class EmailIT extends AbstractAppTest {


	private static final String MARIADB_VERSION = "mariadb:10.6.12";

	@Container
	public static final MariaDBContainer<?> emaildb = new MariaDBContainer<>(DockerImageName.parse(MARIADB_VERSION))
		.withDatabaseName("emailreader")
		.withUsername("root")
		.withPassword("")
		.withInitScript("sql/init-db.sql");

	@Autowired
	EmailRepository emailRepository;

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
	void test1_fetchEmails() throws Exception {

		final var response = setupCall()
			.withServicePath("/email?municipalityId=2281&namespace=myNamespace")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<List<Email>>() {

			});

		assertThat(emaildb.isRunning()).isTrue();
		assertThat(response).isNotNull().hasSize(1).element(0).satisfies(
			email -> {
				assertThat(email.id()).isEqualTo("81471222-5798-11e9-ae24-57fa13b361e1");
				assertThat(email.subject()).isEqualTo("Sample subject");
				assertThat(email.message()).isEqualTo("Hello, this is a sample email.");
				assertThat(email.from()).isEqualTo("fromadress@sundsvall.se");
				assertThat(email.to()).hasSize(2).element(0).satisfies(
					to -> assertThat(to).isEqualTo("recipient1@sundsvall.se"));
				assertThat(email.attachments()).hasSize(1).element(0).satisfies(
					attachment -> {
						assertThat(attachment.content()).isEqualTo("Attachment content");
						assertThat(attachment.name()).isEqualTo("attachment_name.pdf");
						assertThat(attachment.contentType()).isEqualTo("application/pdf");
					});
			}
		);
	}

	@Test
	void test2_deleteEmail() {

		setupCall()
			.withServicePath("/email/81471222-5798-11e9-ae24-57fa13b361e1")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final var result = emailRepository.findAll();

		assertThat(result).isEmpty();

	}

}
