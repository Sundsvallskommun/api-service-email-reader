package se.sundsvall.emailreader.integration.db.entity;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "graph_credentials")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GraphCredentialsEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "tenant_id")
	private String tenantId;

	@Column(name = "client_id")
	private String clientId;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "graph_credentials_email_address",
		joinColumns = @JoinColumn(name = "graph_credentials_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_graph_credentials_email_address_graph_credentials_id")))
	private List<String> emailAddress;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "namespace")
	private String namespace;

	@Column(name = "destination_folder")
	private String destinationFolder;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "graph_credentials_metadata",
		joinColumns = @JoinColumn(name = "graph_credentials_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_graph_credentials_metadata_graph_credentials_id")))
	private Map<String, String> metadata;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}
}
