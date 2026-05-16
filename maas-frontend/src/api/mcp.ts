import client from './client'
import type { ApiResponse } from '../types'

export interface McpServer {
  id: string
  name: string
  description: string | null
  transport: 'stdio' | 'sse'
  command: string | null
  args: string | null
  envJson: string | null
  url: string | null
  apiKey: string | null
  status: 'online' | 'offline' | 'error'
  configJson: string
  createdAt: string
  updatedAt: string
  toolCount: number
}

export interface McpTool {
  id: string
  serverId: string
  name: string
  description: string | null
  inputSchema: string
  enabled: boolean
  autoRegister: boolean
  createdAt: string
  updatedAt: string
}

export interface McpToolDefinition {
  name: string
  description: string | null
  inputSchema: Record<string, any> | null
}

export interface McpToolCallResult {
  content: any
  isError: boolean
}

export const mcpServerApi = {
  list: () => client.get<any, ApiResponse<McpServer[]>>('/mcp/servers'),
  get: (id: string) => client.get<any, ApiResponse<McpServer>>(`/mcp/servers/${id}`),
  create: (data: any) => client.post<any, ApiResponse<McpServer>>('/mcp/servers', data),
  update: (id: string, data: any) => client.put<any, ApiResponse<McpServer>>(`/mcp/servers/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/mcp/servers/${id}`),
  listTools: (id: string) => client.get<any, ApiResponse<McpTool[]>>(`/mcp/servers/${id}/tools`),
  deleteTool: (id: string, toolId: string) =>
    client.delete<any, ApiResponse<null>>(`/mcp/servers/${id}/tools/${toolId}`),
  // Runtime methods
  listRuntimeTools: (id: string) =>
    client.get<any, ApiResponse<McpToolDefinition[]>>(`/mcp/servers/${id}/runtime-tools`),
  executeTool: (serverId: string, toolName: string, args: Record<string, any>) =>
    client.post<any, ApiResponse<McpToolCallResult>>('/mcp/execute', { serverId, toolName, arguments: args }),
  disconnect: (id: string) =>
    client.post<any, ApiResponse<null>>(`/mcp/servers/${id}/disconnect`),
}
