import client from './client'
import type { ApiResponse } from '../types'

export interface Skill {
  id: string
  name: string
  description: string | null
  version: number
  status: 'draft' | 'published' | 'deprecated' | 'retired'
  type: string | null
  configJson: string
  publishNote: string | null
  createdBy: string | null
  createdAt: string
  updatedAt: string
}

export interface ToolDef {
  id: string
  skillId: string | null
  name: string
  description: string | null
  source: 'built_in' | 'mcp' | 'api'
  sourceRef: string | null
  inputSchema: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export const skillApi = {
  list: () => client.get<any, ApiResponse<Skill[]>>('/skills'),
  get: (id: string) => client.get<any, ApiResponse<Skill>>(`/skills/${id}`),
  create: (data: any) => client.post<any, ApiResponse<Skill>>('/skills', data),
  update: (id: string, data: any) => client.put<any, ApiResponse<Skill>>(`/skills/${id}`, data),
  publish: (id: string, note?: string) =>
    client.post<any, ApiResponse<Skill>>(`/skills/${id}/publish`, { note }),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/skills/${id}`),
}

export const toolApi = {
  list: (params?: { q?: string; source?: string; enabled?: boolean }) =>
    client.get<any, ApiResponse<ToolDef[]>>('/tools', { params }),
  get: (id: string) => client.get<any, ApiResponse<ToolDef>>(`/tools/${id}`),
  create: (data: any) => client.post<any, ApiResponse<ToolDef>>('/tools', data),
  update: (id: string, data: any) => client.put<any, ApiResponse<ToolDef>>(`/tools/${id}`, data),
  toggle: (id: string) => client.patch<any, ApiResponse<ToolDef>>(`/tools/${id}/toggle`),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/tools/${id}`),
  listBySkill: (skillId: string) =>
    client.get<any, ApiResponse<ToolDef[]>>(`/skills/${skillId}/tools`),
}
