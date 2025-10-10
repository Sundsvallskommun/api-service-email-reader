alter table if exists email
    add column if not exists html_message longtext;
