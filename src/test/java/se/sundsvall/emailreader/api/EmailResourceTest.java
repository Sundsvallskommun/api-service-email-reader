package se.sundsvall.emailreader.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.emailreader.api.model.Email;
import se.sundsvall.emailreader.service.EmailService;


@ExtendWith(MockitoExtension.class)
class EmailResourceTest {

    @Mock
    EmailService emailService;

    @InjectMocks
    EmailResource emailResource;


    @Test
    void testGetAllEmails() {
        when(emailService.getAllEmails())
            .thenReturn(List.of(Email.builder()
                .withSubject("someSubject")
                .withTo("someTo")
                .withFrom("someFrom").withMessage("someMessage")
                .withMessageID("someMessageId")
                .build()));

        final var result = emailResource.getAllEmails();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody()).isNotNull().isNotEmpty().hasSize(1);

        final var email = result.getBody().get(0);

        assertThat(email).isNotNull();
        assertThat(email.messageID()).isEqualTo("someMessageId");
        assertThat(email.subject()).isEqualTo("someSubject");
        assertThat(email.to()).isEqualTo("someTo");
        assertThat(email.from()).isEqualTo("someFrom");
        assertThat(email.message()).isEqualTo("someMessage");


        verify(emailService, times(1)).getAllEmails();
        verifyNoMoreInteractions(emailService);

    }


    @Test
    void testGetEmail() {

        when(emailService.getEmail(any(String.class)))
            .thenReturn(Email.builder()
                .withSubject("someSubject")
                .withTo("someTo")
                .withFrom("someFrom").withMessage("someMessage")
                .withMessageID("someMessageId")
                .build());

        final var result = emailResource.getEmail("someMessageId");

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        final var email = result.getBody();

        assertThat(email).isNotNull();
        assertThat(email.messageID()).isEqualTo("someMessageId");
        assertThat(email.subject()).isEqualTo("someSubject");
        assertThat(email.to()).isEqualTo("someTo");
        assertThat(email.from()).isEqualTo("someFrom");
        assertThat(email.message()).isEqualTo("someMessage");

        verify(emailService, times(1)).getEmail(any(String.class));
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
