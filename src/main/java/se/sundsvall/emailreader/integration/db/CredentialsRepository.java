package se.sundsvall.emailreader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

@CircuitBreaker(name = "credentialsRepository")
public interface CredentialsRepository extends JpaRepository<CredentialsEntity, String> {

	List<CredentialsEntity> findAllByMunicipalityId(final String municipalityId);

	void deleteByMunicipalityIdAndId(final String municipalityId, final String id);

	Optional<CredentialsEntity> findByMunicipalityIdAndId(final String municipalityId, final String id);

	List<CredentialsEntity> findAllByActionAndEnabled(final String action, boolean enabled);

}
