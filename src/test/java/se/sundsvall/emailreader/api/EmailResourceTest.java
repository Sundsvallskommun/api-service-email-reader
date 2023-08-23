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

    @InjectMocks
    private EmailResource emailResource;

    @Mock
    private EmailService emailService;

    @Test
    void testGetAllEmails() {

        when(emailService.getAllEmails(any(String.class), any(String.class)))
            .thenReturn(List.of(Email.builder()
                .withSubject("someSubject")
                .withTo(List.of("someTo"))
                .withFrom("someFrom").withMessage("someMessage")
                .withId("someId")
                .withAttachments(List.of(Email.Attachment.builder()
                    .withName("someName")
                    .withContent("someContent")
                    .withContentType("someContentType")
                    .build()))
                .build()));

        final var result = emailResource.getAllEmails("someMunicipalityId", "someNamespace");

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();

        assertThat(result.getBody()).isNotNull().isNotEmpty().hasSize(1);

        final var email = result.getBody().get(0);

        assertThat(email).isNotNull();
        assertThat(email.id()).isEqualTo("someId");
        assertThat(email.subject()).isEqualTo("someSubject");
        assertThat(email.to().get(0)).hasSize(1).element(0)
            .satisfies(to -> assertThat(to).isEqualTo("someTo"));
        assertThat(email.from()).isEqualTo("someFrom");
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
