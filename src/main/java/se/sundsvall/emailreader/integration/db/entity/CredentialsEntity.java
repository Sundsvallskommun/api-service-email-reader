package se.sundsvall.emailreader.integration.db.entity;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "credentials")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CredentialsEntity {
	
	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "credentials_email_address",
		joinColumns = @JoinColumn(name = "credentials_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_credentials_email_address_credentials_id")))
	private List<String> emailAddress;

	@Column(name = "domain")
	private String domain;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "destination_folder")
	private String destinationFolder;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "credentials_metadata",
		joinColumns = @JoinColumn(name = "credentials_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_credentials_metadata_credentials_id")))
	private Map<String, String> metadata;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

}
