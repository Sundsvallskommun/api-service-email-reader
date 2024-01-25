package se.sundsvall.emailreader.integration.db.entity;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import org.hibernate.annotations.UuidGenerator;

import se.sundsvall.emailreader.api.model.Header;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_header")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailHeaderEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(name = "header_key")
	private Header header;

	@ElementCollection
	@CollectionTable(name = "email_header_value", joinColumns = @JoinColumn(name = "header_id", referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_header_value_header_id")))
	@Column(name = "value", length = 2048)
	@OrderColumn(name = "order_index")
	private List<String> values;

}
