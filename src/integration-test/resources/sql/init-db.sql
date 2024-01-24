-- Insert into email
INSERT INTO email (created_at, sender, id, municipality_id, namespace, subject, message)
VALUES (CURRENT_TIMESTAMP,
        'fromaddress@sundsvall.se',
        '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281',
        'myNamespace',
        'Sample subject',
        'Hello, this is a sample email.');

-- Insert into email_entity_to
INSERT INTO email_recipient (email_id, recipients)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient1@sundsvall.se');
INSERT INTO email_recipient (email_id, recipients)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient2@sundsvall.se');

-- Insert into attachment
INSERT INTO attachment (created_at, content_type, name, content, email_id)
VALUES (CURRENT_TIMESTAMP,
        'application/pdf',
        'attachment_name.pdf',
        'Attachment content',
        '81471222-5798-11e9-ae24-57fa13b361e1');

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
INSERT INTO credentials_email_address (credentials_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox1@sundsvall.se');
INSERT INTO credentials_email_address (credentials_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox2@sundsvall.se');

INSERT INTO credentials_metadata (credentials_id, metadata_key, metadata)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someKey', 'someValue');

-- Insert into email_header
INSERT INTO email_header (email_id, id, header_key)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', '81471222-5798-11e9-ae24-57fa13b361e1',
        'REFERENCES');

-- Insert into email_header_value
INSERT INTO email_header_value (header_id, value)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someValue'),
       ('81471222-5798-11e9-ae24-57fa13b361e1', 'someOtherValue');
