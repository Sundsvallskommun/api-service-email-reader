ALTER TABLE email_header
    MODIFY header_key ENUM('AUTO_SUBMITTED', 'IN_REPLY_TO', 'MESSAGE_ID', 'REFERENCES');
