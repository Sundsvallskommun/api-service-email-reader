-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate the 'email_attachments' table
TRUNCATE TABLE email_attachments;

-- Truncate the 'email_entity_to' table
TRUNCATE TABLE email_entity_to;

-- Truncate the 'email' table
TRUNCATE TABLE email;

-- Truncate the 'credentials_entity_email_address' table
TRUNCATE TABLE credentials_entity_email_address;

-- Truncate the 'credentials' table
TRUNCATE TABLE credentials;

-- Truncate the 'attachment' table
TRUNCATE TABLE attachment;

TRUNCATE TABLE email_metadata;

TRUNCATE TABLE credentials_metadata;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
