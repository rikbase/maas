package com.maas.dify.repository;

import com.maas.dify.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    Optional<OAuthClient> findByClientId(String clientId);
    List<OAuthClient> findByStatus(String status);
}
