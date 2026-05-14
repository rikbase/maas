CREATE TABLE mcp_server (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(256) NOT NULL,
    description TEXT,
    transport VARCHAR(16) NOT NULL DEFAULT 'stdio',
    command VARCHAR(512),
    args JSONB DEFAULT '[]'::jsonb,
    env_json JSONB DEFAULT '{}'::jsonb,
    url VARCHAR(512),
    api_key VARCHAR(256),
    status VARCHAR(16) NOT NULL DEFAULT 'online',
    config_json JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE mcp_tool (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_id UUID NOT NULL REFERENCES mcp_server(id) ON DELETE CASCADE,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    input_schema JSONB DEFAULT '{}'::jsonb,
    enabled BOOLEAN NOT NULL DEFAULT true,
    auto_register BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_mcp_tool_server_id ON mcp_tool(server_id);
CREATE INDEX idx_mcp_server_status ON mcp_server(status);
