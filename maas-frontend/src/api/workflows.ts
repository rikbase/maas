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

export interface ExecutionTrend {
  date: string
  totalCount: number
  completedCount: number
  failedCount: number
  runningCount: number
  pendingCount: number
  cancelledCount: number
}

export interface ExecutionListParams {
  page?: number
  size?: number
  status?: string
  start?: string
  end?: string
}

// ---- Workflow Triggers ----

export interface WorkflowTrigger {
  id: string
  workflowId: string
  triggerType: 'cron' | 'webhook'
  cronExpression: string | null
  webhookSecret: string | null
  status: string
  lastFiredAt: string | null
  createdAt: string
  updatedAt: string
}

export const triggerApi = {
  list: (workflowId: string) =>
    client.get<any, ApiResponse<WorkflowTrigger[]>>(`/workflows/${workflowId}/triggers`),
  create: (workflowId: string, data: { triggerType: string; cronExpression?: string; webhookSecret?: string }) =>
    client.post<any, ApiResponse<WorkflowTrigger>>(`/workflows/${workflowId}/triggers`, data),
  update: (workflowId: string, triggerId: string, data: { cronExpression?: string; webhookSecret?: string; status?: string }) =>
    client.put<any, ApiResponse<WorkflowTrigger>>(`/workflows/${workflowId}/triggers/${triggerId}`, data),
  delete: (workflowId: string, triggerId: string) =>
    client.delete<any, ApiResponse<null>>(`/workflows/${workflowId}/triggers/${triggerId}`),
}

export const executionApi = {
  list: (params?: ExecutionListParams) => client.get<any, ApiResponse<ExecutionPage>>('/executions', { params }),
  get: (id: string) => client.get<any, ApiResponse<Execution>>(`/executions/${id}`),
  trends: (range = 'week') => client.get<any, ApiResponse<ExecutionTrend[]>>('/executions/trends', { params: { range } }),
}
