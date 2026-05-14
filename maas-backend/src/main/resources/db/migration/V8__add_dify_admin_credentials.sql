ALTER TABLE dify_config
    ADD COLUMN admin_email VARCHAR(255),
    ADD COLUMN admin_password_encrypted VARCHAR(1024);
