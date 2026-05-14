CREATE TABLE maas_login_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(64) NOT NULL,
    ip          VARCHAR(45),
    user_agent  VARCHAR(512),
    success     BOOLEAN NOT NULL DEFAULT true,
    fail_reason VARCHAR(256),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_login_log_created_at ON maas_login_log(created_at DESC);
CREATE INDEX idx_login_log_username ON maas_login_log(username);
