-- Insert into email
INSERT INTO email (created_at, email_from, id, municipality_id, namespace, subject, message)
VALUES (CURRENT_TIMESTAMP,
        'fromaddress@sundsvall.se',
        '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281',
        'myNamespace',
        'Sample subject',
        'Hello, this is a sample email.');

-- Insert into email_entity_to
INSERT INTO email_entity_to (email_entity_id, email_to)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient1@sundsvall.se');
INSERT INTO email_entity_to (email_entity_id, email_to)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient2@sundsvall.se');

-- Insert into attachment
INSERT INTO attachment (created_at, content_type, name, content)
VALUES (CURRENT_TIMESTAMP,
        'application/pdf',
        'attachment_name.pdf',
        'Attachment content');

-- Get the ID of the inserted attachment
SET @attachment_id = LAST_INSERT_ID();

-- Insert into email_attachments
INSERT INTO email_attachments (attachments_id, email_entity_id)
VALUES (@attachment_id, '81471222-5798-11e9-ae24-57fa13b361e1');

INSERT INTO email_metadata (email_id, metadata_key, metadata)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someKey', 'someValue');

-- Insert into credentials
INSERT INTO credentials (created_at,
                         destination_folder,
                         domain,
                         id,
                         municipality_id,
                         namespace,
                         password,
                         username)
VALUES (CURRENT_TIMESTAMP,
        'someDestinationFolder',
        'someDomain',
        '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281',
        'someNamespace',
        'somePassword',
        'someUsername');

-- Insert into credentials_entity_email_address
INSERT INTO credentials_entity_email_address (credentials_entity_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox1@sundsvall.se');
INSERT INTO credentials_entity_email_address (credentials_entity_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox2@sundsvall.se');

INSERT INTO credentials_metadata (credentials_id, metadata_key, metadata)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someKey', 'someValue');
