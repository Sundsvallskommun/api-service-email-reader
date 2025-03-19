package se.sundsvall.emailreader.service.scheduler;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.emailreader.TestUtility.createCredentialsEntity;
import static se.sundsvall.emailreader.TestUtility.createEmailEntity;

import generated.se.sundsvall.messaging.SmsRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.emailreader.integration.ews.EWSIntegration;
import se.sundsvall.emailreader.integration.ews.EWSMapper;
import se.sundsvall.emailreader.integration.messaging.MessagingIntegration;
import se.sundsvall.emailreader.service.EmailService;

@ExtendWith(MockitoExtension.class)
class EwsSchedulerTest {

	@Mock
	private EWSMapper ewsMapperMock;
	@Mock
	private Consumer<String> consumerMock;
	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;
	@Mock
	private EmailService emailServiceMock;

	@Mock
	private EWSIntegration ewsIntegrationMock;

	@Mock
	private MessagingIntegration messagingIntegrationMock;

	@Captor
	private ArgumentCaptor<SmsRequest> smsRequestCaptor;

	@Captor
	private ArgumentCaptor<MessageBody> messageBodyCaptor;

	@InjectMocks
	private EwsScheduler ewsScheduler;

	@Test
	void checkForNewEmails() throws Exception {
		final var emailMessage = mock(EmailMessage.class);
		final var credential = createCredentialsEntity();
		final var emailAddresses = "someEmailAddress";
		final var email = createEmailEntity(emptyMap());
		when(emailServiceMock.findAllByAction("PERSIST")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq(emailAddresses), any())).thenReturn(List.of(emailMessage));
		when(ewsMapperMock.toEmails(any(), any(), any(), any())).thenReturn(List.of(email));
		doNothing().when(emailServiceMock).saveAndMoveEmail(email, emailAddresses, credential);

		ewsScheduler.checkForNewEmails();

		verify(emailServiceMock).findAllByAction("PERSIST");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq(emailAddresses), any());
		verify(emailServiceMock).saveAndMoveEmail(email, emailAddresses, credential);
		verifyNoMoreInteractions(emailServiceMock);
	}

	@Test
	void checkForNewEmails_noCredentials() throws Exception {
		when(emailServiceMock.findAllByAction("PERSIST")).thenReturn(List.of());

		ewsScheduler.checkForNewEmails();

		verify(emailServiceMock).findAllByAction("PERSIST");
		verify(emailServiceMock, never()).getAllEmailsInInbox(any(), any(), any());
		verify(emailServiceMock, never()).saveAndMoveEmail(any(), any(), any());
		verifyNoMoreInteractions(emailServiceMock);
	}

	@Test
	void checkForNewEmails_continuesWhenCheckedException() throws Exception {
		final var emailMessage1 = mock(EmailMessage.class);
		final var emailMessage2 = mock(EmailMessage.class);
		when(emailServiceMock.findAllByAction("PERSIST")).thenReturn(List.of(createCredentialsEntity(), createCredentialsEntity()));
		when(emailServiceMock.getAllEmailsInInbox(any(), any(), any())).thenReturn(List.of(emailMessage1, emailMessage2));
		when(ewsMapperMock.toEmails(any(), any(), any(), any())).thenReturn(List.of(createEmailEntity(emptyMap()), createEmailEntity(emptyMap())));
		doThrow(new Exception("Something is very wrong!")).when(emailServiceMock).saveAndMoveEmail(any(), any(), any());

		ewsScheduler.checkForNewEmails();

		verify(emailServiceMock, times(1)).findAllByAction("PERSIST");
		verify(emailServiceMock, times(2)).getAllEmailsInInbox(any(), any(), any());
		verify(emailServiceMock, times(4)).saveAndMoveEmail(any(), any(), any());
	}

	@Test
	void checkForOldEmailsAndSendReport() {
		doNothing().when(emailServiceMock).sendReport();

		ewsScheduler.checkForOldEmailsAndSendReport();

		verify(emailServiceMock).sendReport();
		verifyNoMoreInteractions(emailServiceMock);
	}

	@Test
	void checkForNewSmsEmails_NoFailures() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailMessage = mock(EmailMessage.class);
		final var emailAddress = mock(EmailAddress.class);
		final var emailMap = Map.of("Message", "someMessage", "Recipient", "07012345678,07112345678");
		final var resultMap = Map.of("VALID", List.of("+467012345678", "+467112345678"));

		when(emailMessage.getReceivedBy()).thenReturn(emailAddress);
		when(emailAddress.getAddress()).thenReturn("someEmailAddress");
		when(emailServiceMock.findAllByAction("SEND_SMS")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any())).thenReturn(List.of(emailMessage));
		when(ewsIntegrationMock.extractValuesEmailMessage(any())).thenReturn(emailMap);
		when(ewsIntegrationMock.validateRecipientNumbers(any())).thenReturn(resultMap);

		ewsScheduler.checkForNewSmsEmails();

		verify(messagingIntegrationMock, times(1)).sendSms(credential.getMunicipalityId(), new SmsRequest().sender("Sundsvall").message("someMessage").mobileNumber("+467012345678"));
		verify(messagingIntegrationMock, times(1)).sendSms(credential.getMunicipalityId(), new SmsRequest().sender("Sundsvall").message("someMessage").mobileNumber("+467112345678"));
		verify(emailServiceMock).findAllByAction("SEND_SMS");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any());
		verify(ewsIntegrationMock).extractValuesEmailMessage(emailMessage);
		verify(ewsIntegrationMock).moveEmail(any(), any(), any());
	}

	@Test
	void checkForNewSmsEmails_throwException() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailMessage = mock(EmailMessage.class);
		final var emailAddress = mock(EmailAddress.class);

		when(emailMessage.getReceivedBy()).thenReturn(emailAddress);
		when(emailAddress.getAddress()).thenReturn("someEmailAddress");
		when(emailServiceMock.findAllByAction("SEND_SMS")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any())).thenReturn(List.of(emailMessage));
		when(ewsIntegrationMock.extractValuesEmailMessage(any())).thenThrow(new RuntimeException("Something went wrong"));
		ewsScheduler.checkForNewSmsEmails();

		verify(emailServiceMock).findAllByAction("SEND_SMS");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any());
		verify(ewsIntegrationMock).extractValuesEmailMessage(emailMessage);
		verify(ewsIntegrationMock).moveEmail(any(), any(), any());
		verifyNoMoreInteractions(emailServiceMock, ewsIntegrationMock);
		verifyNoInteractions(messagingIntegrationMock);
	}

	@Test
	void checkForNewSmsEmails_withoutRecipient() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailMessage = mock(EmailMessage.class);
		final var emailAddress = mock(EmailAddress.class);
		final var emailMap = Map.of("Message", "someMessage");

		when(emailMessage.getReceivedBy()).thenReturn(emailAddress);
		when(emailAddress.getAddress()).thenReturn("someEmailAddress");
		when(emailServiceMock.findAllByAction("SEND_SMS")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any())).thenReturn(List.of(emailMessage));
		when(ewsIntegrationMock.extractValuesEmailMessage(any())).thenReturn(emailMap);

		ewsScheduler.checkForNewSmsEmails();

		verify(emailServiceMock).findAllByAction("SEND_SMS");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any());
		verify(ewsIntegrationMock).extractValuesEmailMessage(emailMessage);
		verify(ewsIntegrationMock).moveEmail(any(), any(), any());
		verifyNoInteractions(messagingIntegrationMock);
	}

	@Test
	void checkForNewSmsEmails_withFailures() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailMessage = mock(EmailMessage.class);
		final var emailAddress = mock(EmailAddress.class);
		final var emailMap = Map.of("Message", "someMessage", "Recipient", "070123456789,071-23456789");
		final var resultMap = Map.of("VALID", List.of("+4670123456789"), "INVALID", List.of("+4671-23456789"));

		when(emailMessage.getReceivedBy()).thenReturn(emailAddress);
		when(emailAddress.getAddress()).thenReturn("someEmailAddress");
		when(emailServiceMock.findAllByAction("SEND_SMS")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any())).thenReturn(List.of(emailMessage));
		when(ewsIntegrationMock.extractValuesEmailMessage(any())).thenReturn(emailMap);
		when(ewsIntegrationMock.validateRecipientNumbers(any())).thenReturn(resultMap);

		ewsScheduler.checkForNewSmsEmails();

		verify(messagingIntegrationMock).sendSms(eq(credential.getMunicipalityId()), smsRequestCaptor.capture());
		assertThat(smsRequestCaptor.getValue()).satisfies(request -> {
			assertThat(request.getSender()).isEqualTo("Sundsvall");
			assertThat(request.getMessage()).isEqualTo("someMessage");
			assertThat(request.getMobileNumber()).isEqualTo("+4670123456789");
		});

		verify(emailServiceMock).findAllByAction("SEND_SMS");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any());
		verify(ewsIntegrationMock).extractValuesEmailMessage(emailMessage);
		verify(ewsIntegrationMock).moveEmail(any(), any(), any());
	}

	@Test
	void checkForNewSmsEmails_onlyFailures() throws Exception {
		final var credential = createCredentialsEntity();
		final var emailMessage = mock(EmailMessage.class);
		final var emailAddress = mock(EmailAddress.class);
		final var emailMap = Map.of("Message", "someMessage", "Recipient", "070123456789123123,071-23456789");
		final var resultMap = Map.of("INVALID", List.of("+4670123456789123123", "+4671-23456789"));

		when(emailMessage.getReceivedBy()).thenReturn(emailAddress);
		when(emailAddress.getAddress()).thenReturn("someEmailAddress");
		when(emailServiceMock.findAllByAction("SEND_SMS")).thenReturn(List.of(credential));
		when(emailServiceMock.getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any())).thenReturn(List.of(emailMessage));
		when(ewsIntegrationMock.extractValuesEmailMessage(any())).thenReturn(emailMap);
		when(ewsIntegrationMock.validateRecipientNumbers(any())).thenReturn(resultMap);

		ewsScheduler.checkForNewSmsEmails();

		verify(emailServiceMock).findAllByAction("SEND_SMS");
		verify(emailServiceMock).getAllEmailsInInbox(eq(credential), eq("someEmailAddress"), any());
		verify(ewsIntegrationMock).extractValuesEmailMessage(emailMessage);
		verify(ewsIntegrationMock).moveEmail(any(), any(), any());
	}

}
