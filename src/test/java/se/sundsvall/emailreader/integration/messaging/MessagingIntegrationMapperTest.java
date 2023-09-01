package se.sundsvall.emailreader.integration.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessagingIntegrationMapperTest {

    @Test
    void toRequest() {

        final var result = MessagingIntegrationMapper.toRequest("someAddress", "some message", "[Warning] EmailReader has detected unhandled emails");

        assertThat(result).isNotNull();
        assertThat(result.getEmailAddress()).isEqualTo("someAddress");
        assertThat(result.getMessage()).isEqualTo("some message");
        assertThat(result.getSubject()).isEqualTo("[Warning] EmailReader has detected unhandled emails");
        assertThat(result.getSender()).isNotNull();
        assertThat(result.getSender().getName()).isEqualTo("EmailReader");
        assertThat(result.getSender().getAddress()).isEqualTo("noreply@emailsender.se");
    }

}
