import client from './client'
import type { ApiResponse } from '../types'

export interface DifyConfig {
  id: string
  name: string
  baseUrl: string
  status: 'connected' | 'disconnected' | 'error'
  adminEmail: string | null
  lastTestAt: string | null
  createdAt: string
  updatedAt: string
}

export interface DifyTestResult {
  connected: boolean
  message: string
  appName?: string
}

export interface AuthorizeResponse {
  code: string
  state: string
  redirectUri: string
}

export const difyApi = {
  list: () => client.get<any, ApiResponse<DifyConfig[]>>('/dify'),
  get: (id: string) => client.get<any, ApiResponse<DifyConfig>>(`/dify/${id}`),
  create: (data: { name: string; baseUrl: string; authCode: string; adminEmail?: string; adminPassword?: string }) =>
    client.post<any, ApiResponse<DifyConfig>>('/dify', data),
  update: (id: string, data: { name?: string; baseUrl?: string; authCode?: string; adminEmail?: string; adminPassword?: string }) =>
    client.put<any, ApiResponse<DifyConfig>>(`/dify/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/dify/${id}`),
  test: (id: string) => client.post<any, ApiResponse<DifyTestResult>>(`/dify/${id}/test`),
  ssoUrl: (id: string) => window.location.origin + '/api/dify/' + id + '/sso',
  oauthAuthorize: (clientId: string, difyConfigId: string) =>
    client.post<any, ApiResponse<AuthorizeResponse>>('/oauth2/authorize', {
      clientId,
      redirectUri: window.location.origin + '/api/oauth2/callback',
      state: difyConfigId,
      difyConfigId,
    }),
}
