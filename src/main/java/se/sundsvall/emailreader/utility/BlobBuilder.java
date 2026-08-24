package se.sundsvall.emailreader.utility;

import java.sql.Blob;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class BlobBuilder {

	public Blob createBlob(final byte[] content) {
		return Hibernate.getLobHelper().createBlob(content);
	}

}
