import client from './client'
import type { ApiResponse } from '../types'

export interface UsageStats {
  requestCount: number
  totalCost: number
  totalTokens: number
  promptTokens: number
  completionTokens: number
  avgLatencyMs: number
  errorCount: number
}

export interface UsageTrend {
  date: string
  requestCount: number
  tokenCount: number
  cost: number
}

export interface UsageRank {
  id: string | null
  name: string
  requestCount: number
  tokenCount: number
  cost: number
}

export interface ProviderHealth {
  providerName: string
  requestCount: number
  errorCount: number
  avgLatencyMs: number
  totalCost: number
}

export const usageApi = {
  stats: () => client.get<any, ApiResponse<UsageStats>>('/usage/stats'),
  trends: (range = 'today') =>
    client.get<any, ApiResponse<UsageTrend[]>>('/usage/trends', { params: { range } }),
  byModel: (range = 'today') =>
    client.get<any, ApiResponse<UsageRank[]>>('/usage/by-model', { params: { range } }),
  byProvider: (range = 'today') =>
    client.get<any, ApiResponse<ProviderHealth[]>>('/usage/by-provider', { params: { range } }),
}
