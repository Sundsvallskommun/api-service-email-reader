package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    EmailService emailService;


    @Test
    void testGetAllEmails() {

        final var result = emailService.getAllEmails();

        assertThat(result).isNotNull().isNotEmpty().hasSize(1);

        final var email = result.get(0);

        assertThat(email).isNotNull();
        assertThat(email.messageID()).isEqualTo("someMessageId");
        assertThat(email.subject()).isEqualTo("someSubject");
        assertThat(email.to()).isEqualTo("someTo");
        assertThat(email.from()).isEqualTo("someFrom");
        assertThat(email.message()).isEqualTo("someMessage");

    }


    @Test
    void getEmailWithMessageId() {

        final var result = emailService.getEmail("someMessageId");

        assertThat(result).isNotNull();
        assertThat(result.messageID()).isEqualTo("someMessageId");
        assertThat(result.subject()).isEqualTo("someSubject");
        assertThat(result.to()).isEqualTo("someTo");
        assertThat(result.from()).isEqualTo("someFrom");
        assertThat(result.message()).isEqualTo("someMessage");
    }

    @Test
    void deleteEmail() {
        emailService.deleteEmail("someMessageId");
    }

}
