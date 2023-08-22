package se.sundsvall.emailreader.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;


    @Test
    void testGetAllEmails() {

        final var result = emailService.getAllEmails("someMunicipalityId", "someNamespace");

        assertThat(result).isNotNull().hasSize(1);

        final var email = result.get(0);

        assertThat(email).isNotNull();
        assertThat(email.id()).isEqualTo("someId");
        assertThat(email.subject()).isEqualTo("someSubject");
        assertThat(email.to()).isEqualTo("someTo");
        assertThat(email.from()).isEqualTo("someFrom");
        assertThat(email.message()).isEqualTo("someMessage");

    }

    @Test
    void deleteEmail() {
        emailService.deleteEmail("someId");
    }

}
