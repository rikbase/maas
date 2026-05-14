import client from './client'
import type { ApiResponse } from '../types'

export interface LoginLogVO {
  id: string
  username: string
  ip: string
  success: boolean
  failReason: string | null
  createdAt: string
}

export interface LoginStatsVO {
  todaySuccess: number
  todayFailed: number
  last24hAttempts: number
}

export const loginLogApi = {
  recent: () => client.get<any, ApiResponse<LoginLogVO[]>>('/logs/login/recent'),
  stats: () => client.get<any, ApiResponse<LoginStatsVO>>('/logs/login/stats'),
}
