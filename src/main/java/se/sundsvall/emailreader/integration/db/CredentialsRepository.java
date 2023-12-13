package se.sundsvall.emailreader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

@CircuitBreaker(name = "credentialsRepository")
public interface CredentialsRepository extends JpaRepository<CredentialsEntity, String> {

}
