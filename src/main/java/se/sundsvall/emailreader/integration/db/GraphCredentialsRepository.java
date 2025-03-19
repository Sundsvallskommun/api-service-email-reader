package se.sundsvall.emailreader.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.emailreader.integration.db.entity.GraphCredentialsEntity;

@CircuitBreaker(name = "graphCredentialsRepository")
public interface GraphCredentialsRepository extends JpaRepository<GraphCredentialsEntity, String> {

	List<GraphCredentialsEntity> findAllByMunicipalityId(final String municipalityId);

	void deleteByMunicipalityIdAndId(final String municipalityId, final String id);

	Optional<GraphCredentialsEntity> findByMunicipalityIdAndId(final String municipalityId, final String id);

}
