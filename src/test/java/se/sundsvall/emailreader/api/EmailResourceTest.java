package se.sundsvall.emailreader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.service.EmailService;


@ExtendWith(MockitoExtension.class)
class EmailResourceTest {

	@InjectMocks
	private EmailResource emailResource;

	@Mock
	private EmailService emailService;

	@Test
	void testGetAllEmails() {

		when(emailService.getAllEmails(any(String.class), any(String.class)))
			.thenReturn(List.of(createEmail()));

		final var result = emailResource.getAllEmails("someMunicipalityId", "someNamespace");

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		assertThat(result.getBody()).isNotNull().isNotEmpty().hasSize(1);

		final var email = result.getBody().getFirst();

		assertThat(email).isNotNull();
		assertThat(email.id()).isEqualTo("someId");
		assertThat(email.subject()).isEqualTo("someSubject");
		assertThat(email.recipients()).hasSize(1).element(0)
			.satisfies(recipient -> assertThat(recipient).isEqualTo("someRecipient"));
		assertThat(email.sender()).isEqualTo("someSender");
		assertThat(email.metadata()).hasSize(1).containsEntry("someKey", "someValue");
		assertThat(email.message()).isEqualTo("someMessage");
		assertThat(email.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment.name()).isEqualTo("someName");
			assertThat(attachment.contentType()).isEqualTo("someContentType");
			assertThat(attachment.content()).isEqualTo("someContent");
		});

		verify(emailService, times(1)).getAllEmails(any(String.class), any(String.class));
		verifyNoMoreInteractions(emailService);

	}

	@Test
	void testDeleteEmail() {

		final var result = emailResource.deleteEmail("someMessageId");

		assertThat(result).isNotNull();
		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

		verify(emailService, times(1)).deleteEmail(any(String.class));
		verifyNoMoreInteractions(emailService);
	}

}
