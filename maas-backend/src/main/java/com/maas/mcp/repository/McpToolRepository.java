package com.maas.mcp.repository;

import com.maas.mcp.entity.McpTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface McpToolRepository extends JpaRepository<McpTool, UUID> {
    List<McpTool> findByServerId(UUID serverId);
    List<McpTool> findByServerIdAndEnabledTrue(UUID serverId);
    long countByServerId(UUID serverId);
}
