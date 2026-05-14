CREATE TABLE workflow (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE workflow_version (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL REFERENCES workflow(id) ON DELETE CASCADE,
    version INTEGER NOT NULL,
    definition_json JSONB NOT NULL DEFAULT '{}',
    status VARCHAR(16) NOT NULL DEFAULT 'draft',
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(workflow_id, version)
);

CREATE TABLE workflow_execution (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL REFERENCES workflow(id) ON DELETE CASCADE,
    version_id UUID REFERENCES workflow_version(id) ON DELETE SET NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    trigger_type VARCHAR(32),
    trigger_info TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE step_execution (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL REFERENCES workflow_execution(id) ON DELETE CASCADE,
    step_id VARCHAR(64) NOT NULL,
    step_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'pending',
    input JSONB,
    output JSONB,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_workflow_version_workflow_id ON workflow_version(workflow_id);
CREATE INDEX idx_workflow_execution_workflow_id ON workflow_execution(workflow_id);
CREATE INDEX idx_step_execution_execution_id ON step_execution(execution_id);
