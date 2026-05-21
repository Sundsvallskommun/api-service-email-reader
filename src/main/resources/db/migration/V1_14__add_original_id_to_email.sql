alter table if exists email
    add column if not exists original_id varchar(255);

alter table if exists email
    add constraint uk_email_original_id_municipality_namespace
        unique (original_id, municipality_id, namespace);
