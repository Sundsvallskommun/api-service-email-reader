package se.sundsvall.emailreader.service.scheduler;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.emailreader.integration.db.GraphCredentialsRepository;
import se.sundsvall.emailreader.integration.graph.GraphIntegration;
import se.sundsvall.emailreader.service.EmailService;

@Component
public class GraphScheduler {
	private static final Logger LOG = LoggerFactory.getLogger(GraphScheduler.class);
	private final Consumer<String> emailSetUnHealthyConsumer;
	private final GraphIntegration graphIntegration;
	private final GraphCredentialsRepository graphCredentialsRepository;
	private final EmailService emailService;

	@Value("${scheduled.check-for-new-emails.graph.name}")
	private String emailJobName;

	public GraphScheduler(final GraphIntegration graphIntegration, final GraphCredentialsRepository graphCredentialsRepository, final EmailService emailService, final Dept44HealthUtility dept44HealthUtility) {
		this.graphIntegration = graphIntegration;
		this.graphCredentialsRepository = graphCredentialsRepository;
		this.emailService = emailService;
		this.emailSetUnHealthyConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(emailJobName, String.format("Email error: %s", msg));

	}

	@Dept44Scheduled(
		cron = "${scheduled.check-for-new-emails.graph.cron}",
		name = "${scheduled.check-for-new-emails.graph.name}",
		lockAtMostFor = "${scheduled.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduled.maximum-execution-time}")
	public void checkForNewEmails() {

		LOG.info("Checking for new emails");
		graphCredentialsRepository.findAll()
			.forEach(

				credential -> credential.getEmailAddress()
					.forEach(emailAddress -> {
						final var emails = graphIntegration.getEmails(emailAddress, credential, emailSetUnHealthyConsumer);
						LOG.info("Fetched {} emails for address '{}'", emails.size(), emailAddress);
						emails.forEach(email -> {
							try {
								emailService.saveEmail(email);

								email.setAttachments(graphIntegration.getAttachments(emailAddress, credential, email.getOriginalId(), emailSetUnHealthyConsumer));
								emailService.saveEmail(email);

							} catch (final Exception e) {
								LOG.error("Failed to handle individual email with id: '{}'. ", email.getId(), e);
								emailSetUnHealthyConsumer.accept("[Graph] Failed to handle individual email");
							} finally {
								graphIntegration.moveEmail(emailAddress, email.getOriginalId(), credential, emailSetUnHealthyConsumer);
							}
						});
					}));

	}
}
