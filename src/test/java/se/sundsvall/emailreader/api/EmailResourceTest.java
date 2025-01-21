package se.sundsvall.emailreader.api;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;
import static se.sundsvall.emailreader.service.mapper.EmailMapper.toEmail;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.emailreader.Application;
import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class EmailResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	@MockitoBean
	private EmailService emailService;

	@Test
	void testGetAllEmails() {

		when(emailService.getAllEmails(any(String.class), any(String.class)))
			.thenReturn(List.of(toEmail(createEmailEntity(emptyMap()))));

		final var response = webTestClient.get()
			.uri("/2281/email/namespace")
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Email.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull().hasSize(1);

		verify(emailService, times(1)).getAllEmails("2281", "namespace");
		verifyNoMoreInteractions(emailService);
	}

	@Test
	void testDeleteEmail() {

		final var uuid = UUID.randomUUID().toString();

		webTestClient.delete()
			.uri("/2281/email/" + uuid)
			.exchange()
			.expectStatus()
			.isNoContent();

		verify(emailService, times(1)).deleteEmail("2281", uuid);
		verifyNoMoreInteractions(emailService);
	}

	@Test
	void testGetAttachment() {
		webTestClient.get()
			.uri("/2281/email/attachments/1")
			.exchange()
			.expectStatus()
			.isOk();

		verify(emailService, times(1)).getMessageAttachmentStreamed(eq(1L), any());
		verifyNoMoreInteractions(emailService);
	}
}
