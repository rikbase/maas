package com.maas.mcp.service;

import com.maas.mcp.dto.*;
import com.maas.mcp.entity.McpServer;
import com.maas.mcp.entity.McpServerStatus;
import com.maas.mcp.repository.McpServerRepository;
import com.maas.mcp.repository.McpToolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class McpServerService {

    private final McpServerRepository serverRepository;
    private final McpToolRepository toolRepository;

    public McpServerService(McpServerRepository serverRepository, McpToolRepository toolRepository) {
        this.serverRepository = serverRepository;
        this.toolRepository = toolRepository;
    }

    public List<McpServerVO> findAll() {
        return serverRepository.findAll().stream()
            .map(s -> McpServerVO.from(s, toolRepository.countByServerId(s.getId())))
            .toList();
    }

    public McpServerVO findById(UUID id) {
        McpServer s = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("McpServer not found: " + id));
        return McpServerVO.from(s, toolRepository.countByServerId(id));
    }

    public McpServerVO create(McpServerCreateRequest req) {
        McpServer s = new McpServer();
        s.setName(req.name());
        s.setDescription(req.description());
        if (req.transport() != null) s.setTransport(req.transport());
        s.setCommand(req.command());
        s.setArgs(req.args() != null ? req.args() : "[]");
        s.setEnvJson(req.envJson() != null ? req.envJson() : "{}");
        s.setUrl(req.url());
        s.setApiKey(req.apiKey());
        s.setConfigJson(req.configJson() != null ? req.configJson() : "{}");
        return McpServerVO.from(serverRepository.save(s), 0);
    }

    public McpServerVO update(UUID id, McpServerUpdateRequest req) {
        McpServer s = serverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("McpServer not found: " + id));
        if (req.name() != null) s.setName(req.name());
        if (req.description() != null) s.setDescription(req.description());
        if (req.transport() != null) s.setTransport(req.transport());
        if (req.command() != null) s.setCommand(req.command());
        if (req.args() != null) s.setArgs(req.args());
        if (req.envJson() != null) s.setEnvJson(req.envJson());
        if (req.url() != null) s.setUrl(req.url());
        if (req.apiKey() != null) s.setApiKey(req.apiKey());
        if (req.status() != null) s.setStatus(req.status());
        if (req.configJson() != null) s.setConfigJson(req.configJson());
        return McpServerVO.from(serverRepository.save(s), toolRepository.countByServerId(id));
    }

    public void delete(UUID id) {
        toolRepository.findByServerId(id).forEach(t -> toolRepository.delete(t));
        serverRepository.deleteById(id);
    }

    // Tools
    public List<McpToolVO> listTools(UUID serverId) {
        return toolRepository.findByServerId(serverId).stream()
            .map(McpToolVO::from)
            .toList();
    }

    public McpToolVO createTool(UUID serverId, McpToolCreateRequest req) {
        var t = new com.maas.mcp.entity.McpTool();
        t.setServerId(serverId);
        t.setName(req.name());
        t.setDescription(req.description());
        t.setInputSchema(req.inputSchema() != null ? req.inputSchema() : "{}");
        t.setAutoRegister(req.autoRegister() != null ? req.autoRegister() : true);
        return McpToolVO.from(toolRepository.save(t));
    }

    public void deleteTool(UUID toolId) {
        toolRepository.deleteById(toolId);
    }
}
