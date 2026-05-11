import client from './client'
import type { Provider, ApiResponse } from '../types'

export const providerApi = {
  list: () => client.get<any, ApiResponse<Provider[]>>('/providers'),
  get: (id: string) => client.get<any, ApiResponse<Provider>>(`/providers/${id}`),
  create: (data: { name: string; type: string; configJson: string }) =>
    client.post<any, ApiResponse<Provider>>('/providers', data),
  update: (id: string, data: { name?: string; configJson?: string }) =>
    client.put<any, ApiResponse<Provider>>(`/providers/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/providers/${id}`),
}
