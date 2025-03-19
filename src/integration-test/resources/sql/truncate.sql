-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE email_recipient;
TRUNCATE TABLE email;
TRUNCATE TABLE credentials_email_address;
TRUNCATE TABLE credentials;
TRUNCATE TABLE attachment;
TRUNCATE TABLE email_metadata;
TRUNCATE TABLE credentials_metadata;
TRUNCATE TABLE email_header_value;
TRUNCATE TABLE email_header;
TRUNCATE TABLE graph_credentials_email_address;
TRUNCATE TABLE graph_credentials_metadata;
TRUNCATE TABLE graph_credentials;
-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
