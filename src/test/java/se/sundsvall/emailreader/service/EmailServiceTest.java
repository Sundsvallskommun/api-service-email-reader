package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.api.model.Header;
import se.sundsvall.emailreader.integration.db.EmailRepository;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private EmailRepository emailRepository;

	@InjectMocks
	private EmailService emailService;

	@Test
	void testGetAllEmails() {

		when(emailRepository.findByMunicipalityIdAndNamespace(any(String.class), any(String.class)))
			.thenReturn(List.of(createEmailEntity()));

		final var result = emailService.getAllEmails("someMunicipalityId", "someNamespace");

		assertThat(result).isNotNull().hasSize(1);

		final var email = result.getFirst();

		assertThat(email).isNotNull();
		assertThat(email.id()).isEqualTo("someId");
		assertThat(email.subject()).isEqualTo("someSubject");
		assertThat(email.recipients()).hasSize(1).element(0).satisfies(recipient ->
			assertThat(recipient).isEqualTo("someRecipient"));
		assertThat(email.sender()).isEqualTo("someSender");
		assertThat(email.message()).isEqualTo("someMessage");
		assertThat(email.headers()).hasSize(3)
			.containsEntry(Header.MESSAGE_ID, List.of("someValue"))
			.containsEntry(Header.REFERENCES, List.of("someReferenceValue"))
			.containsEntry(Header.IN_REPLY_TO, List.of("someReplyToValue"));
		assertThat(email.metadata()).hasSize(1).containsEntry("someKey", "someValue");

		verify(emailRepository, times(1))
			.findByMunicipalityIdAndNamespace(any(String.class), any(String.class));
		verifyNoMoreInteractions(emailRepository);
	}

	@Test
	void deleteEmail() {

		emailService.deleteEmail("someId");

		verify(emailRepository, times(1)).deleteById(any(String.class));
		verifyNoMoreInteractions(emailRepository);
	}

}
