package com.maas.registry.service;

import com.maas.registry.dto.ToolCreateRequest;
import com.maas.registry.dto.ToolUpdateRequest;
import com.maas.registry.dto.ToolVO;
import com.maas.registry.entity.ToolDefinition;
import com.maas.registry.entity.ToolSource;
import com.maas.registry.repository.ToolDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ToolDefinitionService {

    private final ToolDefinitionRepository repository;

    public ToolDefinitionService(ToolDefinitionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ToolVO> listAll() {
        return repository.findAll().stream().map(ToolVO::from).toList();
    }

    @Transactional(readOnly = true)
    public ToolVO findById(UUID id) {
        return repository.findById(id)
            .map(ToolVO::from)
            .orElseThrow(() -> new RuntimeException("Tool not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ToolVO> search(String q, ToolSource source, Boolean enabled) {
        return repository.findAll().stream()
            .filter(t -> q == null || t.getName().toLowerCase().contains(q.toLowerCase())
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q.toLowerCase())))
            .filter(t -> source == null || t.getSource() == source)
            .filter(t -> enabled == null || t.isEnabled() == enabled)
            .map(ToolVO::from)
            .toList();
    }

    public ToolVO create(ToolCreateRequest req) {
        ToolDefinition t = new ToolDefinition();
        t.setName(req.name());
        t.setDescription(req.description());
        t.setSource(req.source() != null ? req.source() : ToolSource.built_in);
        t.setSourceRef(req.sourceRef());
        t.setInputSchema(req.inputSchema() != null ? req.inputSchema() : "{}");
        return ToolVO.from(repository.save(t));
    }

    public ToolVO update(UUID id, ToolUpdateRequest req) {
        ToolDefinition t = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tool not found: " + id));
        if (req.name() != null) t.setName(req.name());
        if (req.description() != null) t.setDescription(req.description());
        if (req.source() != null) t.setSource(req.source());
        if (req.sourceRef() != null) t.setSourceRef(req.sourceRef());
        if (req.inputSchema() != null) t.setInputSchema(req.inputSchema());
        if (req.enabled() != null) t.setEnabled(req.enabled());
        return ToolVO.from(repository.save(t));
    }

    public ToolVO toggleEnabled(UUID id) {
        ToolDefinition t = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tool not found: " + id));
        t.setEnabled(!t.isEnabled());
        return ToolVO.from(repository.save(t));
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
