package com.maas.registry.repository;

import com.maas.registry.entity.ToolDefinition;
import com.maas.registry.entity.ToolSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ToolDefinitionRepository extends JpaRepository<ToolDefinition, UUID> {
    List<ToolDefinition> findBySkillId(UUID skillId);
    List<ToolDefinition> findBySource(ToolSource source);
    List<ToolDefinition> findByEnabledTrue();
}
