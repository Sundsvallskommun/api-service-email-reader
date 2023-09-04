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
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.integration.db.EmailRepository;

@WireMockAppTestSuite(files = "classpath:/EmailIT/", classes = Application.class)
@Sql(scripts = {
	"/sql/truncate.sql",
	"/sql/init-db.sql"
})
class EmailIT extends AbstractAppTest {

	@Autowired
	EmailRepository emailRepository;

	@Test
	void test1_fetchEmails() throws Exception {

		final var response = setupCall()
			.withServicePath("/email?municipalityId=2281&namespace=myNamespace")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<List<Email>>() {

			});

		assertThat(response).isNotNull().hasSize(1).element(0).satisfies(
			email -> {
				assertThat(email.id()).isEqualTo("81471222-5798-11e9-ae24-57fa13b361e1");
				assertThat(email.subject()).isEqualTo("Sample subject");
				assertThat(email.message()).isEqualTo("Hello, this is a sample email.");
				assertThat(email.from()).isEqualTo("fromaddress@sundsvall.se");
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
