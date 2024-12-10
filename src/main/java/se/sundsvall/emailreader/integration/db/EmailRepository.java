package se.sundsvall.emailreader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.emailreader.integration.db.entity.EmailEntity;

@CircuitBreaker(name = "emailRepository")
public interface EmailRepository extends JpaRepository<EmailEntity, String> {

	List<EmailEntity> findByMunicipalityIdAndNamespace(final String municipalityId, final String namespace);

	void deleteByMunicipalityIdAndId(final String municipalityId, final String id);

}
