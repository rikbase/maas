package com.maas.mcp.repository;

import com.maas.mcp.entity.McpServer;
import com.maas.mcp.entity.McpServerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface McpServerRepository extends JpaRepository<McpServer, UUID> {
    List<McpServer> findByStatus(McpServerStatus status);
}
