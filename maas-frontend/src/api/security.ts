import client from './client'
import type { ApiResponse } from '../types'

export interface SecurityRule {
  id: string
  name: string
  description: string | null
  detectorType: 'prompt_injection' | 'secret_leak'
  configJson: string
  scopeJson: string
  severity: 'low' | 'medium' | 'high' | 'critical'
  action: 'block' | 'flag' | 'audit'
  threshold: number
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateRuleRequest {
  name: string
  description?: string
  detectorType: 'prompt_injection' | 'secret_leak'
  configJson?: string
  scopeJson?: string
  severity?: 'low' | 'medium' | 'high' | 'critical'
  action?: 'block' | 'flag' | 'audit'
  threshold?: number
}

export interface SecurityEvent {
  id: string
  ruleId: string | null
  apiKeyId: string | null
  provider: string | null
  model: string | null
  direction: string
  detectorType: string
  severity: string
  actionTaken: string
  requestSummary: string | null
  matchedContent: string | null
  createdAt: string
}

export interface SecurityStats {
  totalEvents: number
  blockedCount: number
  flaggedCount: number
  promptInjectionCount: number
  secretLeakCount: number
  last24hEvents: number
}

export const ruleApi = {
  list: () => client.get<any, ApiResponse<SecurityRule[]>>('/security/rules'),
  get: (id: string) => client.get<any, ApiResponse<SecurityRule>>(`/security/rules/${id}`),
  create: (data: CreateRuleRequest) =>
    client.post<any, ApiResponse<SecurityRule>>('/security/rules', data),
  update: (id: string, data: Partial<CreateRuleRequest & { enabled: boolean }>) =>
    client.put<any, ApiResponse<SecurityRule>>(`/security/rules/${id}`, data),
  toggleEnabled: (id: string) =>
    client.patch<any, ApiResponse<SecurityRule>>(`/security/rules/${id}/toggle`),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/security/rules/${id}`),
}

export const eventApi = {
  list: (params?: { severity?: string; detectorType?: string; start?: string; end?: string; page?: number; size?: number }) =>
    client.get<any, any>('/security/events', { params }),
  stats: () => client.get<any, ApiResponse<SecurityStats>>('/security/events/stats'),
}
