package se.sundsvall.emailreader;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.emailreader.integration.ews.LenientXmlInputFactory;

import static org.springframework.boot.SpringApplication.run;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
public class Application {
	public static void main(final String... args) {
		LenientXmlInputFactory.install();
		run(Application.class, args);
	}
}
