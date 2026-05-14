import client from './client'
import type { Provider, ApiResponse } from '../types'

export interface ModelDef {
  id: string
  modelName: string
  providerId: string
  providerName: string
  capabilities: string
  status: string
}

export const providerApi = {
  list: () => client.get<any, ApiResponse<Provider[]>>('/providers'),
  get: (id: string) => client.get<any, ApiResponse<Provider>>(`/providers/${id}`),
  create: (data: { name: string; type: string; configJson: string }) =>
    client.post<any, ApiResponse<Provider>>('/providers', data),
  update: (id: string, data: { name?: string; configJson?: string }) =>
    client.put<any, ApiResponse<Provider>>(`/providers/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/providers/${id}`),
  checkHealth: (id: string) =>
    client.post<any, ApiResponse<Provider>>(`/providers/${id}/health-check`),
  setHealthStatus: (id: string, healthStatus: string) =>
    client.put<any, ApiResponse<Provider>>(`/providers/${id}/health-status?healthStatus=${healthStatus}`),
}

export const modelApi = {
  list: () => client.get<any, ApiResponse<ModelDef[]>>('/models'),
  get: (id: string) => client.get<any, ApiResponse<ModelDef>>(`/models/${id}`),
  create: (data: { providerId: string; modelName: string; capabilities?: string; status?: string }) =>
    client.post<any, ApiResponse<ModelDef>>('/models', data),
  update: (id: string, data: { modelName?: string; capabilities?: string; status?: string }) =>
    client.put<any, ApiResponse<ModelDef>>(`/models/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/models/${id}`),
}
