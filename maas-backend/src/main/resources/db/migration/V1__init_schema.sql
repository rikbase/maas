CREATE TABLE provider (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(128) NOT NULL,
    type        VARCHAR(64) NOT NULL,
    config_json JSONB NOT NULL DEFAULT '{}',
    status      VARCHAR(32) NOT NULL DEFAULT 'enabled',
    health_status VARCHAR(32) DEFAULT 'unknown',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE provider_model (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL REFERENCES provider(id) ON DELETE CASCADE,
    model_name  VARCHAR(256) NOT NULL,
    capabilities JSONB NOT NULL DEFAULT '[]',
    status      VARCHAR(32) NOT NULL DEFAULT 'active',
    UNIQUE(provider_id, model_name)
);

CREATE TABLE api_key (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_hash    VARCHAR(256) NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
    key_type    VARCHAR(32) NOT NULL DEFAULT 'application',
    policy_json JSONB NOT NULL DEFAULT '{}',
    created_by  VARCHAR(128),
    expires_at  TIMESTAMPTZ,
    status      VARCHAR(32) NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE usage_record (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_key_id        UUID REFERENCES api_key(id),
    provider_id       UUID REFERENCES provider(id),
    model             VARCHAR(256),
    prompt_tokens     INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    latency_ms        INTEGER DEFAULT 0,
    cost              DECIMAL(12,6) DEFAULT 0,
    status            VARCHAR(32) NOT NULL DEFAULT 'success',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usage_created_at ON usage_record(created_at);
CREATE INDEX idx_usage_api_key_id ON usage_record(api_key_id);
CREATE INDEX idx_usage_provider_id ON usage_record(provider_id);
CREATE INDEX idx_provider_model_provider ON provider_model(provider_id);
