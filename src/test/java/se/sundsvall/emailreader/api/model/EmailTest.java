package se.sundsvall.emailreader.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailTest {


    @Test
    void testBuilderAndGetters() {
        
        final var result = Email.builder().withMessageID("someMessageId")
            .withSubject("someSubject")
            .withTo("someTo")
            .withFrom("someFrom")
            .withMessage("someMessage")
            .build();

        assertThat(result).isNotNull();
        assertThat(result.messageID()).isEqualTo("someMessageId");
        assertThat(result.subject()).isEqualTo("someSubject");
        assertThat(result.to()).isEqualTo("someTo");
        assertThat(result.from()).isEqualTo("someFrom");
        assertThat(result.message()).isEqualTo("someMessage");

    }

}
