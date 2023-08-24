package se.sundsvall.emailreader.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;


class EmailTest {

    @Test
    void testBuilderAndGetters() {

        final var result = Email.builder()
            .withId("someId")
            .withSubject("someSubject")
            .withTo(List.of("someTo"))
            .withFrom("someFrom")
            .withMessage("someMessage")
            .withAttachments(List.of(Email.Attachment.builder()
                .withName("someName")
                .withContent("someContent")
                .withContentType("someContentType")
                .build()))
            .build();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("someId");
        assertThat(result.subject()).isEqualTo("someSubject");
        assertThat(result.to()).hasSize(1).element(0).satisfies(to ->
            assertThat(to).isEqualTo("someTo"));
        assertThat(result.from()).isEqualTo("someFrom");
        assertThat(result.message()).isEqualTo("someMessage");
        assertThat(result.attachments()).hasSize(1).element(0).satisfies(attachment -> {
            assertThat(attachment.name()).isEqualTo("someName");
            assertThat(attachment.contentType()).isEqualTo("someContentType");
            assertThat(attachment.content()).isEqualTo("someContent");
        });

    }

}
