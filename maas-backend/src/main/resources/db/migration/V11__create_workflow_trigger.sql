CREATE TABLE workflow_trigger (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id      UUID NOT NULL REFERENCES workflow(id) ON DELETE CASCADE,
    trigger_type     VARCHAR(16) NOT NULL,
    cron_expression  VARCHAR(128),
    webhook_secret   VARCHAR(256),
    status           VARCHAR(16) NOT NULL DEFAULT 'active',
    last_fired_at    TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_workflow_trigger_workflow_id ON workflow_trigger(workflow_id);
