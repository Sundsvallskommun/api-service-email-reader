create table attachment
(
    created_at   datetime(6)  null,
    id           bigint auto_increment
        primary key,
    content_type varchar(255) null,
    name         varchar(255) null,
    content      longtext     null
);

create table credentials
(
    created_at         datetime(6)  null,
    destination_folder varchar(255) null,
    domain             varchar(255) null,
    id                 varchar(255) not null
        primary key,
    municipality_id    varchar(255) null,
    namespace          varchar(255) null,
    password           varchar(255) null,
    username           varchar(255) null
);

create table credentials_entity_email_adress
(
    credentials_entity_id varchar(255) not null,
    email_adress          varchar(255) null
);



create table email
(
    created_at      datetime(6)  null,
    email_from      varchar(255) null,
    id              varchar(255) not null
        primary key,
    municipality_id varchar(255) null,
    namespace       varchar(255) null,
    subject         varchar(255) null,
    message         longtext     null
);

create table email_attachments
(
    attachments_id  bigint       not null,
    email_entity_id varchar(255) not null,
    constraint attachments_id_unique
        unique (attachments_id),
    constraint attachments_id_fk
        foreign key (attachments_id) references attachment (id),
    constraint email_entity_id_fk
        foreign key (email_entity_id) references email (id)
);

create table email_entity_to
(
    email_entity_id varchar(255) not null,
    email_to        varchar(255) null
);


-- Insert into email
INSERT INTO email (created_at, email_from, id, municipality_id, namespace, subject,
                   message)
VALUES (CURRENT_TIMESTAMP, 'fromadress@sundsvall.se', '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281', 'myNamespace', 'Sample subject', 'Hello, this is a sample email.');

INSERT INTO credentials_entity_email_adress (credentials_entity_id, email_adress)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox1@sundsvall.se');
INSERT INTO credentials_entity_email_adress (credentials_entity_id, email_adress)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox2@sundsvall.se');

-- Insert into email_entity_to
INSERT INTO email_entity_to (email_entity_id, email_to)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient1@sundsvall.se');
INSERT INTO email_entity_to (email_entity_id, email_to)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient2@sundsvall.se');

-- Insert into attachment
INSERT INTO attachment (created_at, content_type, name, content)
VALUES (CURRENT_TIMESTAMP, 'application/pdf', 'attachment_name.pdf', 'Attachment content');

-- Get the ID of the inserted attachment
SET
    @attachment_id = LAST_INSERT_ID();

-- Insert into email_attachments
INSERT INTO email_attachments (attachments_id, email_entity_id)
VALUES (@attachment_id, '81471222-5798-11e9-ae24-57fa13b361e1');


-- Insert into credentials
INSERT INTO credentials(created_at, destination_folder, domain, id, municipality_id,
                        namespace, password, username)
VALUES (CURRENT_TIMESTAMP, 'someDestinationFolder', 'someDomain',
        '81471222-5798-11e9-ae24-57fa13b361e1', '2281', 'someNamespace', 'somePassword',
        'someUsername')
