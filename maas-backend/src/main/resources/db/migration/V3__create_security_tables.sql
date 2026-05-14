CREATE TABLE security_rule (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(256) NOT NULL,
    description    TEXT,
    detector_type  VARCHAR(64) NOT NULL,
    config_json    JSONB NOT NULL DEFAULT '{}',
    scope_json     JSONB NOT NULL DEFAULT '{}',
    severity       VARCHAR(16) NOT NULL DEFAULT 'medium',
    action         VARCHAR(16) NOT NULL DEFAULT 'block',
    threshold      DOUBLE PRECISION NOT NULL DEFAULT 0.85,
    enabled        BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE security_event (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id          UUID REFERENCES security_rule(id) ON DELETE SET NULL,
    api_key_id       UUID,
    provider         VARCHAR(128),
    model            VARCHAR(256),
    direction        VARCHAR(16) NOT NULL,
    detector_type    VARCHAR(64) NOT NULL,
    severity         VARCHAR(16) NOT NULL,
    action_taken     VARCHAR(16) NOT NULL,
    request_summary  TEXT,
    matched_content  TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_security_event_created_at ON security_event(created_at);
CREATE INDEX idx_security_event_severity ON security_event(severity);
CREATE INDEX idx_security_event_detector ON security_event(detector_type);
CREATE INDEX idx_security_rule_enabled ON security_rule(enabled);
