package se.sundsvall.emailreader.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.emailreader.integration.db.entity.CredentialsEntity;

public interface CredentialsRepository extends JpaRepository<CredentialsEntity, String> {

}
