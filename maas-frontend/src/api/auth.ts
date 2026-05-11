import client from './client'
import type { ApiResponse } from '../types'

export interface UserVO {
  id: string
  username: string
  displayName: string | null
  role: 'admin' | 'viewer'
}

export interface LoginResponse {
  token: string
  user: UserVO
}

export const authApi = {
  login: (username: string, password: string) =>
    client.post<any, ApiResponse<LoginResponse>>('/auth/login', { username, password }),
  me: () =>
    client.get<any, ApiResponse<UserVO>>('/auth/me'),
}
