package se.sundsvall.emailreader.service.scheduler;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.emailreader.integration.db.GraphCredentialsRepository;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;
import se.sundsvall.emailreader.integration.graph.GraphIntegration;
import se.sundsvall.emailreader.service.EmailService;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphSchedulerTest {

	@Mock
	private GraphIntegration graphIntegration;

	@Mock
	private GraphCredentialsRepository graphCredentialsRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private Dept44HealthUtility dept44HealthUtility;

	@InjectMocks
	private GraphScheduler graphScheduler;

	@Test
	void checkForNewEmails() {
		// Arrange
		final var userId = "test@example.com";
		final var messageId = "messageId";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.build();

		final var email = EmailEntity.builder().withOriginalId(messageId).build();
		final var emails = List.of(email);

		when(graphCredentialsRepository.findAll()).thenReturn(List.of(credentials));
		when(graphIntegration.getEmails(eq(userId), eq(credentials), any())).thenReturn(emails);
		when(graphIntegration.getAttachments(eq(userId), eq(credentials), eq(messageId), any())).thenReturn(List.of());

		// Act
		graphScheduler.checkForNewEmails();

		// Assert
		verify(graphCredentialsRepository).findAll();
		verify(graphIntegration).getEmails(eq(userId), eq(credentials), any());
		verify(emailService, times(2)).saveEmail(email);
		verify(graphIntegration).moveEmail(eq(userId), eq(email.getOriginalId()), eq(credentials), any());

		verifyNoMoreInteractions(graphCredentialsRepository, emailService, graphIntegration, dept44HealthUtility);
	}

	@Test
	void continuesWhenCheckedException() {
		// Arrange
		final var userId = "test@example.com";
		final var messageId = "messageId";
		final var messageId2 = "messageId2";
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var clientId = "clientId";
		final var clientSecret = "clientSecret";
		final var tenantId = "tenantId";
		final Map<String, String> metadata = emptyMap();
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId(clientId)
			.withClientSecret(clientSecret)
			.withTenantId(tenantId)
			.withEmailAddress(List.of(userId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMetadata(metadata)
			.build();

		final var email = EmailEntity.builder().withOriginalId(messageId).build();

		final var email2 = EmailEntity.builder().withOriginalId(messageId2).build();
		final var emails = List.of(email, email2);

		when(graphCredentialsRepository.findAll()).thenReturn(List.of(credentials));
		when(graphIntegration.getEmails(eq(userId), eq(credentials), any())).thenReturn(emails);
		when(graphIntegration.getAttachments(eq(userId), eq(credentials), eq(messageId), any())).thenThrow(new RuntimeException("Test exception"));
		when(graphIntegration.getAttachments(eq(userId), eq(credentials), eq(messageId2), any())).thenReturn(List.of());
		// Act
		graphScheduler.checkForNewEmails();

		// Assert
		verify(graphCredentialsRepository).findAll();
		verify(graphIntegration).getEmails(eq(userId), eq(credentials), any());
		verify(emailService).saveEmail(email);
		verify(emailService, times(2)).saveEmail(email2);
		verify(graphIntegration).moveEmail(eq(userId), eq(email.getOriginalId()), eq(credentials), any());
		verify(graphIntegration).moveEmail(eq(userId), eq(email2.getOriginalId()), eq(credentials), any());
		verify(dept44HealthUtility).setHealthIndicatorUnhealthy(null, "Email error: [Graph] Failed to handle individual email");
		verifyNoMoreInteractions(graphCredentialsRepository, emailService, graphIntegration, dept44HealthUtility);
	}

	@Test
	void checkForNewEmails_firstSaveFailed_doesNotMove() {
		final var userId = "test@example.com";
		final var messageId = "messageId";
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId("c").withClientSecret("s").withTenantId("t")
			.withEmailAddress(List.of(userId))
			.withMunicipalityId("m").withNamespace("n").withMetadata(emptyMap())
			.build();
		final var email = EmailEntity.builder().withOriginalId(messageId).build();

		when(graphCredentialsRepository.findAll()).thenReturn(List.of(credentials));
		when(graphIntegration.getEmails(eq(userId), eq(credentials), any())).thenReturn(List.of(email));
		when(emailService.saveEmail(email)).thenThrow(new RuntimeException("DB constraint violation"));

		graphScheduler.checkForNewEmails();

		verify(emailService).saveEmail(email);
		verify(graphIntegration, never()).getAttachments(any(), any(), any(), any());
		verify(graphIntegration, never()).moveEmail(any(), any(), any(), any());
		verify(dept44HealthUtility).setHealthIndicatorUnhealthy(null, "Email error: [Graph] Failed to handle individual email");
	}

	@Test
	void checkForNewEmails_moveFailedAfterPersist_doesNotPropagate() {
		final var userId = "test@example.com";
		final var messageId = "messageId";
		final var credentials = GraphCredentialsEntity.builder()
			.withClientId("c").withClientSecret("s").withTenantId("t")
			.withEmailAddress(List.of(userId))
			.withMunicipalityId("m").withNamespace("n").withMetadata(emptyMap())
			.build();
		final var email = EmailEntity.builder().withOriginalId(messageId).build();

		when(graphCredentialsRepository.findAll()).thenReturn(List.of(credentials));
		when(graphIntegration.getEmails(eq(userId), eq(credentials), any())).thenReturn(List.of(email));
		when(graphIntegration.getAttachments(eq(userId), eq(credentials), eq(messageId), any())).thenReturn(List.of());
		doThrow(new RuntimeException("Graph move failed"))
			.when(graphIntegration).moveEmail(eq(userId), eq(messageId), eq(credentials), any());

		graphScheduler.checkForNewEmails();

		verify(emailService, times(2)).saveEmail(email);
		verify(graphIntegration).moveEmail(eq(userId), eq(messageId), eq(credentials), any());
		verify(dept44HealthUtility).setHealthIndicatorUnhealthy(null, "Email error: [Graph] Failed to move email after successful persistence");
	}
}
