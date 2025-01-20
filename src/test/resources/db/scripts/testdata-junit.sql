INSERT INTO credentials(id, namespace, municipality_id, action, destination_folder)
VALUES (1, "namespace-1", "municipality_id-1", "PERSIST", "destination_folder-1");

INSERT INTO credentials_metadata(credentials_id, metadata_key, metadata)
VALUES (1, "key", "value");
