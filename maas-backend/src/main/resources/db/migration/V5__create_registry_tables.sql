CREATE TABLE skill (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(128) NOT NULL,
    description TEXT,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'draft',
    type VARCHAR(64),
    config_json JSONB DEFAULT '{}'::jsonb,
    publish_note TEXT,
    created_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tool_definition (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skill_id UUID REFERENCES skill(id) ON DELETE SET NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    source VARCHAR(16) NOT NULL DEFAULT 'built_in',
    source_ref VARCHAR(256),
    input_schema JSONB DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_skill_status ON skill(status);
CREATE INDEX idx_tool_definition_skill_id ON tool_definition(skill_id);
CREATE INDEX idx_tool_definition_source ON tool_definition(source);
