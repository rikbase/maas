package com.maas.dify.repository;

import com.maas.dify.entity.DifyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DifyConfigRepository extends JpaRepository<DifyConfig, UUID> {
}
