package com.maas.registry.repository;

import com.maas.registry.entity.Skill;
import com.maas.registry.entity.SkillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    List<Skill> findByStatus(SkillStatus status);
}
