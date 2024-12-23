package se.sundsvall.emailreader.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.EmailRepository;

@WireMockAppTestSuite(files = "classpath:/EmailIT/", classes = Application.class)
@Sql(scripts = {
	"/sql/truncate.sql",
	"/sql/init-db.sql"
})
class EmailIT extends AbstractAppTest {

	private static final String EMAIL_PATH_TEMPLATE = "/%s/email/%s";

	@Autowired
	private EmailRepository emailRepository;

	@Test
	void test1_fetchEmails() throws Exception {

		final var response = setupCall()
			.withServicePath(EMAIL_PATH_TEMPLATE.formatted("2281", "myNamespace"))
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
				assertThat(email.sender()).isEqualTo("fromaddress@sundsvall.se");
				assertThat(email.metadata()).hasSize(1).containsEntry("someKey", "someValue");
				assertThat(email.recipients()).hasSize(2).element(0).satisfies(
					recipient -> assertThat(recipient).isEqualTo("recipient1@sundsvall.se"));
				assertThat(email.attachments()).hasSize(1).element(0).satisfies(
					attachment -> {
						assertThat(attachment.id()).isEqualTo(1);
						assertThat(attachment.name()).isEqualTo("test_image.png");
						assertThat(attachment.contentType()).isEqualTo("image/png");
					});
				assertThat(email.headers()).hasSize(1).containsEntry(Header.REFERENCES, List.of("someValue", "someOtherValue"));
			});
	}

	@Test
	void test2_deleteEmail() {

		setupCall()
			.withServicePath(EMAIL_PATH_TEMPLATE.formatted("2281", "81471222-5798-11e9-ae24-57fa13b361e1"))
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		final var result = emailRepository.findAll();

		assertThat(result).isEmpty();
	}

	@Test
	void test3_getAttachment() throws IOException {
		setupCall()
			.withServicePath("/1984/email/attachments/1")
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequestAndVerifyResponse();
	}

}
