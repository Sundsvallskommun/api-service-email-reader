CREATE TABLE email_cc_recipient (
    email_id    VARCHAR(255) NOT NULL,
    cc_recipients VARCHAR(255),
    CONSTRAINT fk_email_cc_recipient_email_id FOREIGN KEY (email_id) REFERENCES email (id)
) ENGINE = InnoDB;
