CREATE TABLE dify_config (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    api_key_encrypted VARCHAR(1024) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'disconnected',
    last_test_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
