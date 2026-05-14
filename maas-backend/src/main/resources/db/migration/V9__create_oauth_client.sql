CREATE TABLE oauth_client (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id     VARCHAR(64)  NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL,
    client_name   VARCHAR(128) NOT NULL,
    redirect_uris TEXT         NOT NULL,
    grant_types   VARCHAR(64)  NOT NULL DEFAULT 'authorization_code',
    scopes        VARCHAR(255) NOT NULL DEFAULT 'openid profile',
    status        VARCHAR(16)  NOT NULL DEFAULT 'active',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
