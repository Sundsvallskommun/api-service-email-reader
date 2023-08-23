package se.sundsvall.emailreader.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;

@ExtendWith({MockitoExtension.class})
class EWSMapperTest {

    @Mock
    private ExchangeService mockExchangeService;

    @InjectMocks
    private EWSMapper mapper;

    @Test
    void testToEmail() throws Exception {

        when(mockExchangeService.getRequestedServerVersion())
            .thenReturn(ExchangeVersion.Exchange2010_SP2);

        final var emailMessage = new EmailMessage(mockExchangeService);
        emailMessage.setFrom(new EmailAddress("Test testorsson", "sender@example.com"));
        emailMessage.setSubject("Test Email Subject");
        emailMessage.getToRecipients().add("recipient@example.com");
        emailMessage.setBody(new MessageBody("Mocked email body"));

        final var result = mapper.toEmail(mockExchangeService, emailMessage);

        assertThat(result.from()).isEqualTo("sender@example.com");
        assertThat(result.to().get(0)).isEqualTo("recipient@example.com");
        assertThat(result.subject()).isEqualTo("Test Email Subject");
        assertThat(result.message()).isEqualTo("Mocked email body");
        assertThat(result.id()).isNotNull().isNotEmpty();
    }

}
