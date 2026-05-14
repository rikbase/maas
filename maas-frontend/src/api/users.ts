import client from './client'
import type { ApiResponse } from '../types'

export interface UserVO {
  id: string
  username: string
  displayName: string | null
  role: 'admin' | 'viewer'
  status: 'active' | 'disabled'
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  password: string
  displayName?: string
  role?: string
}

export interface UpdateUserRequest {
  displayName?: string
  password?: string
  role?: string
  status?: string
}

export const userApi = {
  list: () => client.get<any, ApiResponse<UserVO[]>>('/users'),
  get: (id: string) => client.get<any, ApiResponse<UserVO>>(`/users/${id}`),
  create: (data: CreateUserRequest) =>
    client.post<any, ApiResponse<UserVO>>('/users', data),
  update: (id: string, data: UpdateUserRequest) =>
    client.put<any, ApiResponse<UserVO>>(`/users/${id}`, data),
  delete: (id: string) =>
    client.delete<any, ApiResponse<null>>(`/users/${id}`),
}
