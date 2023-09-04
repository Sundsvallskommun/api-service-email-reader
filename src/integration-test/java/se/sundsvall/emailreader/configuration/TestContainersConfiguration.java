package se.sundsvall.emailreader.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

	@Bean
	@ServiceConnection
	MariaDBContainer<?> mariaDBContainer() {
		
		return new MariaDBContainer<>("mariadb:10.6.12")
			.withInitScript("sql/init-db.sql");
	}

}
