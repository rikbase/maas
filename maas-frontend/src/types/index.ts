export interface Provider {
  id: string
  name: string
  type: 'openai_compatible' | 'anthropic' | 'vllm' | 'ollama' | 'custom'
  status: 'enabled' | 'disabled' | 'error'
  healthStatus: string
  createdAt: string
  updatedAt: string
}

export interface ApiKey {
  id: string
  name: string
  keyType: 'root' | 'team' | 'application'
  keyPrefix: string
  rawKey?: string
  status: string
  createdAt: string
  expiresAt: string | null
}

export interface CreateKeyRequest {
  name: string
  keyType: 'root' | 'team' | 'application'
  policyJson?: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
