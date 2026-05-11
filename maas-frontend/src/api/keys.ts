import client from './client'
import type { ApiKey, CreateKeyRequest, ApiResponse } from '../types'

export const keyApi = {
  list: () => client.get<any, ApiResponse<ApiKey[]>>('/keys'),
  create: (data: CreateKeyRequest) =>
    client.post<any, ApiResponse<ApiKey>>('/keys', data),
  revoke: (id: string) =>
    client.post<any, ApiResponse<null>>(`/keys/${id}/revoke`),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/keys/${id}`),
}
