package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.integration.db.EmailRepository;
import se.sundsvall.emailreader.integration.db.entity.AttachmentEntity;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.service.mapper.EmailMapper;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Spy
	private EmailMapper mapper;

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

		final var email = result.get(0);

		assertThat(email).isNotNull();
		assertThat(email.id()).isEqualTo("someId");
		assertThat(email.subject()).isEqualTo("someSubject");
		assertThat(email.recipients()).hasSize(1).element(0).satisfies(recipient ->
			assertThat(recipient).isEqualTo("someRecipient"));
		assertThat(email.sender()).isEqualTo("someSender");
		assertThat(email.message()).isEqualTo("someMessage");
		assertThat(email.metadata()).hasSize(1).containsEntry("someKey", "someValue");

		verify(mapper, times(1)).toEmails(anyList());
		verify(mapper, times(1)).toEmail(any(EmailEntity.class));
		verify(mapper, times(1)).toAttachment(any(AttachmentEntity.class));
		verify(emailRepository, times(1))
			.findByMunicipalityIdAndNamespace(any(String.class), any(String.class));
		verifyNoMoreInteractions(emailRepository);
		verifyNoMoreInteractions(mapper);
	}

	@Test
	void deleteEmail() {

		emailService.deleteEmail("someId");

		verify(emailRepository, times(1)).deleteById(any(String.class));
		verifyNoMoreInteractions(emailRepository);
		verifyNoInteractions(mapper);
	}

}
