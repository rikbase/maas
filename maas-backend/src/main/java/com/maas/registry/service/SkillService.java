package com.maas.registry.service;

import com.maas.registry.dto.*;
import com.maas.registry.entity.Skill;
import com.maas.registry.entity.SkillStatus;
import com.maas.registry.repository.SkillRepository;
import com.maas.registry.repository.ToolDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;
    private final ToolDefinitionRepository toolRepository;
    private final ToolDefinitionService toolService;

    public SkillService(SkillRepository skillRepository, ToolDefinitionRepository toolRepository,
                        ToolDefinitionService toolService) {
        this.skillRepository = skillRepository;
        this.toolRepository = toolRepository;
        this.toolService = toolService;
    }

    public List<SkillVO> findAll() {
        return skillRepository.findAll().stream()
            .map(SkillVO::from)
            .toList();
    }

    public SkillVO findById(UUID id) {
        return skillRepository.findById(id)
            .map(SkillVO::from)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + id));
    }

    public SkillVO create(SkillCreateRequest req) {
        Skill s = new Skill();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setType(req.type());
        s.setConfigJson(req.configJson() != null ? req.configJson() : "{}");
        return SkillVO.from(skillRepository.save(s));
    }

    public SkillVO update(UUID id, SkillUpdateRequest req) {
        Skill s = skillRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + id));
        if (req.name() != null) s.setName(req.name());
        if (req.description() != null) s.setDescription(req.description());
        if (req.type() != null) s.setType(req.type());
        if (req.configJson() != null) s.setConfigJson(req.configJson());
        if (req.status() != null) s.setStatus(req.status());
        if (req.publishNote() != null) s.setPublishNote(req.publishNote());
        return SkillVO.from(skillRepository.save(s));
    }

    public SkillVO publish(UUID id, String note) {
        Skill s = skillRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + id));
        if (s.getStatus() == SkillStatus.published) {
            s.setVersion(s.getVersion() + 1);
        }
        s.setStatus(SkillStatus.published);
        if (note != null) s.setPublishNote(note);
        return SkillVO.from(skillRepository.save(s));
    }

    public void delete(UUID id) {
        skillRepository.deleteById(id);
    }

    // Tools — delegated to ToolDefinitionService
    public List<ToolVO> listTools() {
        return toolService.listAll();
    }

    public List<ToolVO> listToolsBySkill(UUID skillId) {
        return toolRepository.findBySkillId(skillId).stream()
            .map(ToolVO::from)
            .toList();
    }

    public ToolVO createTool(ToolCreateRequest req) {
        return toolService.create(req);
    }

    public void deleteTool(UUID id) {
        toolService.delete(id);
    }
}
