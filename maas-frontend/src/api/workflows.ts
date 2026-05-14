import client from './client'
import type { ApiResponse } from '../types'

export interface Workflow {
  id: string
  name: string
  description: string | null
  status: 'draft' | 'published' | 'archived'
  createdAt: string
  updatedAt: string
  latestVersion: number | null
  lastRunStatus: string | null
}

export interface WorkflowVersion {
  id: string
  workflowId: string
  version: number
  definitionJson: string
  status: 'draft' | 'published' | 'archived'
  createdBy: string | null
  createdAt: string
}

export interface StepExecution {
  id: string
  executionId: string
  stepId: string
  stepType: 'start' | 'end' | 'ai_call' | 'tool' | 'skill' | 'condition' | 'loop'
  status: 'pending' | 'running' | 'completed' | 'failed' | 'skipped'
  input: string | null
  output: string | null
  errorMessage: string | null
  startedAt: string | null
  finishedAt: string | null
}

export interface Execution {
  id: string
  workflowId: string
  workflowName: string | null
  versionId: string
  status: 'pending' | 'running' | 'completed' | 'failed' | 'cancelled'
  triggerType: string | null
  triggerInfo: string | null
  startedAt: string | null
  finishedAt: string | null
  createdAt: string
  stepExecutions: StepExecution[]
}

export interface ExecutionPage {
  content: Execution[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export const workflowApi = {
  list: () => client.get<any, ApiResponse<Workflow[]>>('/workflows'),
  get: (id: string) => client.get<any, ApiResponse<Workflow>>(`/workflows/${id}`),
  create: (data: { name: string; description?: string; definitionJson?: string }) =>
    client.post<any, ApiResponse<Workflow>>('/workflows', data),
  update: (id: string, data: { name?: string; description?: string; definitionJson?: string }) =>
    client.put<any, ApiResponse<Workflow>>(`/workflows/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/workflows/${id}`),
  publish: (id: string) => client.post<any, ApiResponse<WorkflowVersion>>(`/workflows/${id}/publish`),
  getVersions: (id: string) => client.get<any, ApiResponse<WorkflowVersion[]>>(`/workflows/${id}/versions`),
  rollback: (id: string, version: number) =>
    client.post<any, ApiResponse<WorkflowVersion>>(`/workflows/${id}/rollback`, { version }),
  execute: (id: string, triggerData?: Record<string, any>) =>
    client.post<any, ApiResponse<Execution>>(`/workflows/${id}/execute`, triggerData ?? {}),
  getExecutions: (id: string, params?: { page?: number; size?: number; status?: string; start?: string; end?: string }) =>
    client.get<any, ApiResponse<ExecutionPage>>(`/workflows/${id}/executions`, { params }),
}

export interface ExecutionListParams {
  page?: number
  size?: number
  status?: string
  start?: string
  end?: string
}

export const executionApi = {
  list: (params?: ExecutionListParams) => client.get<any, ApiResponse<ExecutionPage>>('/executions', { params }),
  get: (id: string) => client.get<any, ApiResponse<Execution>>(`/executions/${id}`),
}
