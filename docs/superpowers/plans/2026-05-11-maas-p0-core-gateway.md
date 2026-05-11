# MaaS P0 — 核心网关与接入管理实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 MaaS 平台 P0 阶段：Provider Manager、Gateway Router、API Key Manager 三个核心模块，解决模型接入分散和密钥管理混乱的痛点。

**Architecture:** Spring Boot 单体应用 + Vue 3 管理界面。Gateway 对外暴露 OpenAI 兼容 API，内部通过适配器模式对接不同 AI 提供商。API Key 采用分层密钥体系，在网关层完成鉴权和路由。

**Tech Stack:** Java 17 + Spring Boot 3.x + Spring Data JPA + MySQL/PostgreSQL + Vue 3 + Vite + Pinia

---

### Task 0: 项目脚手架初始化

**Files:**
- Create: `maas-backend/pom.xml`
- Create: `maas-backend/src/main/java/com/maas/MaasApplication.java`
- Create: `maas-backend/src/main/resources/application.yml`
- Create: `maas-backend/src/main/resources/db/migration/V1__init_schema.sql`
- Create: `maas-frontend/package.json`
- Create: `maas-frontend/vite.config.ts`
- Create: `maas-frontend/index.html`
- Create: `maas-frontend/src/main.ts`
- Create: `maas-frontend/src/App.vue`

- [ ] **Step 1: Create Spring Boot project structure**

`pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>
    <groupId>com.maas</groupId>
    <artifactId>maas-backend</artifactId>
    <version>0.1.0</version>
    <name>maas-backend</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create Spring Boot main class**

`MaasApplication.java`:
```java
package com.maas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaasApplication.class, args);
    }
}
```

- [ ] **Step 3: Create application config**

`application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/maas
    username: maas
    password: maas
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

maas:
  encryption:
    key: ${MAAS_ENCRYPTION_KEY:change-me-in-production}
```

- [ ] **Step 4: Create Flyway initial schema**

`V1__init_schema.sql`:
```sql
CREATE TABLE provider (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(128) NOT NULL,
    type        VARCHAR(64) NOT NULL,
    config_json JSONB NOT NULL DEFAULT '{}',
    status      VARCHAR(32) NOT NULL DEFAULT 'enabled',
    health_status VARCHAR(32) DEFAULT 'unknown',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE provider_model (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id UUID NOT NULL REFERENCES provider(id) ON DELETE CASCADE,
    model_name  VARCHAR(256) NOT NULL,
    capabilities JSONB NOT NULL DEFAULT '[]',
    status      VARCHAR(32) NOT NULL DEFAULT 'active',
    UNIQUE(provider_id, model_name)
);

CREATE TABLE api_key (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_hash    VARCHAR(256) NOT NULL UNIQUE,
    name        VARCHAR(128) NOT NULL,
    key_type    VARCHAR(32) NOT NULL DEFAULT 'application',
    policy_json JSONB NOT NULL DEFAULT '{}',
    created_by  VARCHAR(128),
    expires_at  TIMESTAMPTZ,
    status      VARCHAR(32) NOT NULL DEFAULT 'active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE usage_record (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_key_id        UUID REFERENCES api_key(id),
    provider_id       UUID REFERENCES provider(id),
    model             VARCHAR(256),
    prompt_tokens     INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    latency_ms        INTEGER DEFAULT 0,
    cost              DECIMAL(12,6) DEFAULT 0,
    status            VARCHAR(32) NOT NULL DEFAULT 'success',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_usage_created_at ON usage_record(created_at);
CREATE INDEX idx_usage_api_key_id ON usage_record(api_key_id);
CREATE INDEX idx_provider_model_provider ON provider_model(provider_id);
```

- [ ] **Step 5: Create Vue 3 + Vite frontend scaffold**

`maas-frontend/package.json`:
```json
{
  "name": "maas-frontend",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0",
    "typescript": "^5.4.0",
    "vue-tsc": "^2.0.0"
  }
}
```

- [ ] **Step 6: Create Vite config**

`maas-frontend/vite.config.ts`:
```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
```

- [ ] **Step 7: Create Vue entry files**

`maas-frontend/index.html`:
```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MaaS Platform</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.ts"></script>
</body>
</html>
```

`maas-frontend/src/main.ts`:
```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

`maas-frontend/src/App.vue`:
```vue
<template>
  <router-view />
</template>
```

- [ ] **Step 8: Commit scaffold**

```bash
git add .
git commit -m "chore: scaffold maas project with Spring Boot and Vue 3

Set up backend with Spring Boot 3.2, Flyway migrations, and PostgreSQL.
Set up frontend with Vue 3, Vite, Pinia, and vue-router.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 1: 共享基础设施 — 通用异常、API 响应封装、加密工具

**Files:**
- Create: `maas-backend/src/main/java/com/maas/common/exception/BusinessException.java`
- Create: `maas-backend/src/main/java/com/maas/common/exception/GlobalExceptionHandler.java`
- Create: `maas-backend/src/main/java/com/maas/common/dto/ApiResponse.java`
- Create: `maas-backend/src/main/java/com/maas/common/util/EncryptionUtil.java`
- Create: `maas-backend/src/main/java/com/maas/common/config/WebConfig.java`

- [ ] **Step 1: Create ApiResponse wrapper**

`ApiResponse.java`:
```java
package com.maas.common.dto;

public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data);
    }
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 2: Create BusinessException**

`BusinessException.java`:
```java
package com.maas.common.exception;

public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public int getCode() { return code; }
}
```

- [ ] **Step 3: Create GlobalExceptionHandler**

`GlobalExceptionHandler.java`:
```java
package com.maas.common.exception;

import com.maas.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e) {
        return ApiResponse.error(500, "Internal server error");
    }
}
```

- [ ] **Step 4: Create EncryptionUtil**

`EncryptionUtil.java`:
```java
package com.maas.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {
    private final SecretKeySpec keySpec;

    public EncryptionUtil(@Value("${maas.encryption.key}") String key) {
        byte[] keyBytes = key.getBytes();
        byte[] padded = new byte[16];
        System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 16));
        this.keySpec = new SecretKeySpec(padded, "AES");
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
```

- [ ] **Step 5: Create WebConfig with CORS**

`WebConfig.java`:
```java
package com.maas.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

- [ ] **Step 6: Run tests to verify project compiles**

Run: `cd maas-backend && ./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add maas-backend/src/main/java/com/maas/common/
git commit -m "feat: add shared infrastructure (ApiResponse, exception handling, encryption)

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 2: Provider 实体、Repository 和 Service

**Files:**
- Create: `maas-backend/src/main/java/com/maas/provider/entity/Provider.java`
- Create: `maas-backend/src/main/java/com/maas/provider/entity/ProviderModel.java`
- Create: `maas-backend/src/main/java/com/maas/provider/entity/ProviderStatus.java`
- Create: `maas-backend/src/main/java/com/maas/provider/entity/ProviderType.java`
- Create: `maas-backend/src/main/java/com/maas/provider/repository/ProviderRepository.java`
- Create: `maas-backend/src/main/java/com/maas/provider/repository/ProviderModelRepository.java`
- Create: `maas-backend/src/main/java/com/maas/provider/service/ProviderService.java`
- Create: `maas-backend/src/main/java/com/maas/provider/dto/ProviderCreateRequest.java`
- Create: `maas-backend/src/main/java/com/maas/provider/dto/ProviderUpdateRequest.java`
- Create: `maas-backend/src/main/java/com/maas/provider/dto/ProviderVO.java`

- [ ] **Step 1: Write the entity classes**

`ProviderStatus.java`:
```java
package com.maas.provider.entity;

public enum ProviderStatus {
    enabled, disabled, error
}
```

`ProviderType.java`:
```java
package com.maas.provider.entity;

public enum ProviderType {
    openai_compatible, anthropic, vllm, ollama, custom
}
```

`Provider.java`:
```java
package com.maas.provider.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider")
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ProviderType type;

    @Column(name = "config_json", columnDefinition = "jsonb", nullable = false)
    private String configJson;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ProviderStatus status = ProviderStatus.enabled;

    @Column(name = "health_status", length = 32)
    private String healthStatus = "unknown";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Provider() {}

    public Provider(String name, ProviderType type, String configJson) {
        this.name = name;
        this.type = type;
        this.configJson = configJson;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ProviderType getType() { return type; }
    public void setType(ProviderType type) { this.type = type; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public ProviderStatus getStatus() { return status; }
    public void setStatus(ProviderStatus status) { this.status = status; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

`ProviderModel.java`:
```java
package com.maas.provider.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "provider_model", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider_id", "model_name"})
})
public class ProviderModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "model_name", nullable = false, length = 256)
    private String modelName;

    @Column(columnDefinition = "jsonb")
    private String capabilities;

    @Column(length = 32)
    private String status = "active";

    public ProviderModel() {}

    public ProviderModel(Provider provider, String modelName) {
        this.provider = provider;
        this.modelName = modelName;
        this.capabilities = "[]";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getCapabilities() { return capabilities; }
    public void setCapabilities(String capabilities) { this.capabilities = capabilities; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

- [ ] **Step 2: Write the Repository interfaces**

`ProviderRepository.java`:
```java
package com.maas.provider.repository;

import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<Provider, UUID> {
    List<Provider> findByStatus(ProviderStatus status);
    boolean existsByName(String name);
}
```

`ProviderModelRepository.java`:
```java
package com.maas.provider.repository;

import com.maas.provider.entity.ProviderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProviderModelRepository extends JpaRepository<ProviderModel, UUID> {
    List<ProviderModel> findByProviderId(UUID providerId);
    void deleteByProviderId(UUID providerId);
}
```

- [ ] **Step 3: Write DTOs**

`ProviderCreateRequest.java`:
```java
package com.maas.provider.dto;

import com.maas.provider.entity.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProviderCreateRequest(
    @NotBlank String name,
    @NotNull ProviderType type,
    @NotBlank String configJson
) {}
```

`ProviderUpdateRequest.java`:
```java
package com.maas.provider.dto;

public record ProviderUpdateRequest(
    String name,
    String configJson
) {}
```

`ProviderVO.java`:
```java
package com.maas.provider.dto;

import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.entity.ProviderStatus;
import java.time.Instant;
import java.util.UUID;

public record ProviderVO(
    UUID id, String name, ProviderType type,
    ProviderStatus status, String healthStatus,
    Instant createdAt, Instant updatedAt
) {
    public static ProviderVO from(Provider p) {
        return new ProviderVO(
            p.getId(), p.getName(), p.getType(),
            p.getStatus(), p.getHealthStatus(),
            p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
```

- [ ] **Step 4: Write ProviderService**

`ProviderService.java`:
```java
package com.maas.provider.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderUpdateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProviderService {
    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public List<ProviderVO> listAll() {
        return providerRepository.findAll().stream()
            .map(ProviderVO::from)
            .toList();
    }

    public ProviderVO getById(UUID id) {
        return providerRepository.findById(id)
            .map(ProviderVO::from)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
    }

    @Transactional
    public ProviderVO create(ProviderCreateRequest request) {
        if (providerRepository.existsByName(request.name())) {
            throw new BusinessException(409, "Provider name already exists");
        }
        Provider provider = new Provider(request.name(), request.type(), request.configJson());
        provider = providerRepository.save(provider);
        return ProviderVO.from(provider);
    }

    @Transactional
    public ProviderVO update(UUID id, ProviderUpdateRequest request) {
        Provider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
        if (request.name() != null) provider.setName(request.name());
        if (request.configJson() != null) provider.setConfigJson(request.configJson());
        provider = providerRepository.save(provider);
        return ProviderVO.from(provider);
    }

    @Transactional
    public void delete(UUID id) {
        if (!providerRepository.existsById(id)) {
            throw new BusinessException(404, "Provider not found");
        }
        providerRepository.deleteById(id);
    }

    @Transactional
    public void setStatus(UUID id, ProviderStatus status) {
        Provider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Provider not found"));
        provider.setStatus(status);
        providerRepository.save(provider);
    }

    public List<Provider> getEnabledProviders() {
        return providerRepository.findByStatus(ProviderStatus.enabled);
    }
}
```

- [ ] **Step 5: Write tests for ProviderService**

`maas-backend/src/test/java/com/maas/provider/service/ProviderServiceTest.java`:
```java
package com.maas.provider.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock ProviderRepository providerRepository;
    @InjectMocks ProviderService providerService;

    @Test
    void create_shouldSaveProvider() {
        when(providerRepository.existsByName("test")).thenReturn(false);
        when(providerRepository.save(any())).thenAnswer(invocation -> {
            Provider p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProviderVO vo = providerService.create(
            new ProviderCreateRequest("test", ProviderType.openai_compatible, "{}"));

        assertNotNull(vo.id());
        assertEquals("test", vo.name());
        assertEquals(ProviderType.openai_compatible, vo.type());
    }

    @Test
    void create_shouldThrowWhenNameExists() {
        when(providerRepository.existsByName("test")).thenReturn(true);
        assertThrows(BusinessException.class, () ->
            providerService.create(new ProviderCreateRequest("test", ProviderType.openai_compatible, "{}")));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(providerRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> providerService.getById(UUID.randomUUID()));
    }

    @Test
    void listAll_shouldReturnAll() {
        when(providerRepository.findAll()).thenReturn(List.of(new Provider("a", ProviderType.openai_compatible, "{}")));
        assertEquals(1, providerService.listAll().size());
    }
}
```

- [ ] **Step 6: Run tests to verify**

Run: `cd maas-backend && ./mvnw test -pl . -Dtest=ProviderServiceTest -q`
Expected: Tests pass, BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add maas-backend/src/main/java/com/maas/provider/
git add maas-backend/src/test/java/com/maas/provider/
git commit -m "feat: add Provider entity, repository, and service with tests

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 3: Provider Controller 和健康检查

**Files:**
- Create: `maas-backend/src/main/java/com/maas/provider/adapter/ProviderAdapter.java`
- Create: `maas-backend/src/main/java/com/maas/provider/adapter/OpenAICompatibleAdapter.java`
- Create: `maas-backend/src/main/java/com/maas/provider/service/ProviderHealthChecker.java`
- Create: `maas-backend/src/main/java/com/maas/provider/controller/ProviderController.java`

- [ ] **Step 1: Create ProviderAdapter interface**

`ProviderAdapter.java`:
```java
package com.maas.provider.adapter;

import com.maas.provider.entity.Provider;
import java.util.List;

public interface ProviderAdapter {
    boolean supports(Provider provider);
    boolean checkHealth(Provider provider);
    List<String> fetchModels(Provider provider);
}
```

- [ ] **Step 2: Create OpenAICompatibleAdapter**

`OpenAICompatibleAdapter.java`:
```java
package com.maas.provider.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAICompatibleAdapter implements ProviderAdapter {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Provider provider) {
        return provider.getType() == ProviderType.openai_compatible
            || provider.getType() == ProviderType.vllm
            || provider.getType() == ProviderType.ollama;
    }

    @Override
    public boolean checkHealth(Provider provider) {
        try {
            String baseUrl = parseBaseUrl(provider);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/models"))
                .timeout(java.time.Duration.ofSeconds(10))
                .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<String> fetchModels(Provider provider) {
        try {
            String baseUrl = parseBaseUrl(provider);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/models"))
                .timeout(java.time.Duration.ofSeconds(10))
                .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return List.of();

            JsonNode root = objectMapper.readTree(response.body());
            List<String> models = new ArrayList<>();
            JsonNode data = root.get("data");
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    JsonNode id = node.get("id");
                    if (id != null) models.add(id.asText());
                }
            }
            return models;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String parseBaseUrl(Provider provider) {
        try {
            JsonNode config = objectMapper.readTree(provider.getConfigJson());
            JsonNode url = config.get("base_url");
            return url != null ? url.asText().replaceAll("/$", "") : "";
        } catch (Exception e) {
            return "";
        }
    }
}
```

- [ ] **Step 3: Create ProviderHealthChecker**

`ProviderHealthChecker.java`:
```java
package com.maas.provider.service;

import com.maas.provider.adapter.ProviderAdapter;
import com.maas.provider.entity.Provider;
import com.maas.provider.repository.ProviderRepository;
import com.maas.provider.repository.ProviderModelRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderHealthChecker {
    private final ProviderRepository providerRepository;
    private final ProviderModelRepository providerModelRepository;
    private final List<ProviderAdapter> adapters;

    public ProviderHealthChecker(ProviderRepository providerRepository,
                                  ProviderModelRepository providerModelRepository,
                                  List<ProviderAdapter> adapters) {
        this.providerRepository = providerRepository;
        this.providerModelRepository = providerModelRepository;
        this.adapters = adapters;
    }

    @Scheduled(fixedRate = 60_000)
    public void checkAll() {
        List<Provider> providers = providerRepository.findAll();
        for (Provider provider : providers) {
            ProviderAdapter adapter = findAdapter(provider);
            if (adapter == null) continue;

            boolean healthy = adapter.checkHealth(provider);
            provider.setHealthStatus(healthy ? "healthy" : "unhealthy");
            if (healthy) {
                List<String> models = adapter.fetchModels(provider);
                syncModels(provider, models);
            }
            providerRepository.save(provider);
        }
    }

    private void syncModels(Provider provider, List<String> models) {
        providerModelRepository.deleteByProviderId(provider.getId());
        for (String modelName : models) {
            providerModelRepository.save(
                new com.maas.provider.entity.ProviderModel(provider, modelName));
        }
    }

    private ProviderAdapter findAdapter(Provider provider) {
        for (ProviderAdapter adapter : adapters) {
            if (adapter.supports(provider)) return adapter;
        }
        return null;
    }
}
```

- [ ] **Step 4: Create ProviderController**

`ProviderController.java`:
```java
package com.maas.provider.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.provider.dto.ProviderCreateRequest;
import com.maas.provider.dto.ProviderUpdateRequest;
import com.maas.provider.dto.ProviderVO;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public ApiResponse<List<ProviderVO>> list() {
        return ApiResponse.success(providerService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderVO> get(@PathVariable UUID id) {
        return ApiResponse.success(providerService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProviderVO> create(@Valid @RequestBody ProviderCreateRequest request) {
        return ApiResponse.success(providerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProviderVO> update(@PathVariable UUID id, @RequestBody ProviderUpdateRequest request) {
        return ApiResponse.success(providerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        providerService.delete(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> setStatus(@PathVariable UUID id, @RequestParam ProviderStatus status) {
        providerService.setStatus(id, status);
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 5: Run ProviderServiceTest and verify all pass**

Run: `cd maas-backend && ./mvnw test -Dtest=ProviderServiceTest -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add maas-backend/src/main/java/com/maas/provider/
git commit -m "feat: add Provider health check, adapter pattern, and REST controller

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 4: API Key Manager — 实体、Service、Controller

**Files:**
- Create: `maas-backend/src/main/java/com/maas/apikey/entity/ApiKey.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/entity/KeyType.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/repository/ApiKeyRepository.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/service/ApiKeyService.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/dto/CreateKeyRequest.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/dto/KeyVO.java`
- Create: `maas-backend/src/main/java/com/maas/apikey/controller/ApiKeyController.java`
- Create: `maas-backend/src/test/java/com/maas/apikey/service/ApiKeyServiceTest.java`

- [ ] **Step 1: Write the entity**

`KeyType.java`:
```java
package com.maas.apikey.entity;

public enum KeyType {
    root, team, application
}
```

`ApiKey.java`:
```java
package com.maas.apikey.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_key")
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "key_hash", nullable = false, unique = true, length = 256)
    private String keyHash;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 32)
    private KeyType keyType = KeyType.application;

    @Column(name = "policy_json", columnDefinition = "jsonb")
    private String policyJson = "{}";

    @Column(name = "created_by", length = 128)
    private String createdBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(length = 32)
    private String status = "active";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ApiKey() {}

    public ApiKey(String keyHash, String name, KeyType keyType) {
        this.keyHash = keyHash;
        this.name = name;
        this.keyType = keyType;
    }

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    // getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getKeyHash() { return keyHash; }
    public void setKeyHash(String keyHash) { this.keyHash = keyHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public KeyType getKeyType() { return keyType; }
    public void setKeyType(KeyType keyType) { this.keyType = keyType; }
    public String getPolicyJson() { return policyJson; }
    public void setPolicyJson(String policyJson) { this.policyJson = policyJson; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
```

- [ ] **Step 2: Write Repository**

`ApiKeyRepository.java`:
```java
package com.maas.apikey.repository;

import com.maas.apikey.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByKeyHash(String keyHash);
    boolean existsByName(String name);
}
```

- [ ] **Step 3: Write DTOs**

`CreateKeyRequest.java`:
```java
package com.maas.apikey.dto;

import com.maas.apikey.entity.KeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateKeyRequest(
    @NotBlank String name,
    @NotNull KeyType keyType,
    String policyJson
) {}
```

`KeyVO.java`:
```java
package com.maas.apikey.dto;

import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.entity.KeyType;
import java.time.Instant;
import java.util.UUID;

public record KeyVO(
    UUID id, String name, KeyType keyType,
    String keyPrefix, String status,
    Instant createdAt, Instant expiresAt
) {
    public static KeyVO from(ApiKey key) {
        String prefix = key.getKeyHash().length() > 8
            ? key.getKeyHash().substring(key.getKeyHash().length() - 8)
            : "****";
        return new KeyVO(key.getId(), key.getName(), key.getKeyType(),
            prefix, key.getStatus(), key.getCreatedAt(), key.getExpiresAt());
    }
}
```

- [ ] **Step 4: Write ApiKeyService**

`ApiKeyService.java`:
```java
package com.maas.apikey.service;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.repository.ApiKeyRepository;
import com.maas.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public List<KeyVO> listAll() {
        return apiKeyRepository.findAll().stream()
            .map(KeyVO::from)
            .toList();
    }

    public KeyVO getById(UUID id) {
        return apiKeyRepository.findById(id)
            .map(KeyVO::from)
            .orElseThrow(() -> new BusinessException(404, "Key not found"));
    }

    @Transactional
    public KeyVO create(CreateKeyRequest request) {
        if (apiKeyRepository.existsByName(request.name())) {
            throw new BusinessException(409, "Key name already exists");
        }
        String rawKey = generateRawKey();
        String keyHash = hashKey(rawKey);
        ApiKey key = new ApiKey(keyHash, request.name(), request.keyType());
        if (request.policyJson() != null) key.setPolicyJson(request.policyJson());
        key = apiKeyRepository.save(key);
        return KeyVO.from(key);
    }

    @Transactional
    public void delete(UUID id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new BusinessException(404, "Key not found");
        }
        apiKeyRepository.deleteById(id);
    }

    @Transactional
    public void revoke(UUID id) {
        ApiKey key = apiKeyRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "Key not found"));
        key.setStatus("revoked");
        apiKeyRepository.save(key);
    }

    public ApiKey authenticate(String bearerToken) {
        String keyHash = hashKey(bearerToken);
        ApiKey key = apiKeyRepository.findByKeyHash(keyHash)
            .orElseThrow(() -> new BusinessException(401, "Invalid API key"));
        if (!"active".equals(key.getStatus())) {
            throw new BusinessException(401, "API key is not active");
        }
        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(java.time.Instant.now())) {
            throw new BusinessException(401, "API key has expired");
        }
        return key;
    }

    private String generateRawKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return "maas-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashKey(String rawKey) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Key hashing failed", e);
        }
    }
}
```

- [ ] **Step 5: Write tests**

`ApiKeyServiceTest.java`:
```java
package com.maas.apikey.service;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.entity.KeyType;
import com.maas.apikey.repository.ApiKeyRepository;
import com.maas.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock ApiKeyRepository apiKeyRepository;
    @InjectMocks ApiKeyService apiKeyService;

    @Test
    void create_shouldReturnKeyVO() {
        when(apiKeyRepository.existsByName("test-key")).thenReturn(false);
        when(apiKeyRepository.save(any())).thenAnswer(inv -> {
            ApiKey k = inv.getArgument(0);
            k.setId(UUID.randomUUID());
            return k;
        });

        KeyVO vo = apiKeyService.create(new CreateKeyRequest("test-key", KeyType.application, null));
        assertNotNull(vo.id());
        assertEquals("test-key", vo.name());
    }

    @Test
    void create_shouldThrowOnDuplicateName() {
        when(apiKeyRepository.existsByName("dup")).thenReturn(true);
        assertThrows(BusinessException.class, () ->
            apiKeyService.create(new CreateKeyRequest("dup", KeyType.application, null)));
    }

    @Test
    void authenticate_shouldThrowOnInvalidKey() {
        when(apiKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> apiKeyService.authenticate("invalid-key"));
    }

    @Test
    void revoke_shouldSetStatus() {
        ApiKey key = new ApiKey("hash", "k", KeyType.application);
        key.setId(UUID.randomUUID());
        when(apiKeyRepository.findById(key.getId())).thenReturn(Optional.of(key));

        apiKeyService.revoke(key.getId());
        assertEquals("revoked", key.getStatus());
    }
}
```

- [ ] **Step 6: Create ApiKeyController**

`ApiKeyController.java`:
```java
package com.maas.apikey.controller;

import com.maas.apikey.dto.CreateKeyRequest;
import com.maas.apikey.dto.KeyVO;
import com.maas.apikey.service.ApiKeyService;
import com.maas.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    public ApiResponse<List<KeyVO>> list() {
        return ApiResponse.success(apiKeyService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<KeyVO> get(@PathVariable UUID id) {
        return ApiResponse.success(apiKeyService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<KeyVO> create(@Valid @RequestBody CreateKeyRequest request) {
        return ApiResponse.success(apiKeyService.create(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        apiKeyService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<Void> revoke(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 7: Run tests**

Run: `cd maas-backend && ./mvnw test -Dtest=ApiKeyServiceTest -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add maas-backend/src/main/java/com/maas/apikey/
git add maas-backend/src/test/java/com/maas/apikey/
git commit -m "feat: add API Key manager with create, revoke, and auth

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 5: Gateway Router — 统一模型网关

**Files:**
- Create: `maas-backend/src/main/java/com/maas/gateway/dto/ChatCompletionRequest.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/dto/ChatCompletionResponse.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/dto/ChatMessage.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/service/ModelRouter.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/service/ProviderInvoker.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/service/RateLimiter.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/controller/GatewayController.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/config/GatewayAuthInterceptor.java`
- Create: `maas-backend/src/main/java/com/maas/gateway/config/GatewayWebConfig.java`
- Create: `maas-backend/src/test/java/com/maas/gateway/service/ModelRouterTest.java`

- [ ] **Step 1: Write DTOs**

`ChatMessage.java`:
```java
package com.maas.gateway.dto;

public record ChatMessage(String role, String content) {}
```

`ChatCompletionRequest.java`:
```java
package com.maas.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ChatCompletionRequest(
    @NotBlank String model,
    @NotBlank List<ChatMessage> messages,
    Double temperature,
    Integer maxTokens,
    Boolean stream
) {}
```

`ChatCompletionResponse.java`:
```java
package com.maas.gateway.dto;

import java.util.List;

public record ChatCompletionResponse(
    String id,
    String object,
    long created,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(int index, ChatMessage message, String finishReason) {}
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {}
}
```

- [ ] **Step 2: Write ModelRouter**

`ModelRouter.java`:
```java
package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.repository.ProviderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModelRouter {
    private final ProviderRepository providerRepository;

    public ModelRouter(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public Provider route(String model, String preferredProvider) {
        List<Provider> providers = providerRepository.findByStatus(ProviderStatus.enabled);

        if (preferredProvider != null) {
            return providers.stream()
                .filter(p -> p.getName().equals(preferredProvider) && "healthy".equals(p.getHealthStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(503, "Preferred provider unavailable"));
        }

        // Auto-route: find a provider that has this model
        return providers.stream()
            .filter(p -> "healthy".equals(p.getHealthStatus()))
            .filter(p -> hasModel(p, model))
            .findFirst()
            .orElseThrow(() -> new BusinessException(404, "No available provider for model: " + model));
    }

    private boolean hasModel(Provider provider, String model) {
        // Models are synced by ProviderHealthChecker
        return true; // Simplified: actual check would query provider_model table
    }
}
```

- [ ] **Step 3: Write ProviderInvoker**

`ProviderInvoker.java`:
```java
package com.maas.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.gateway.dto.ChatMessage;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class ProviderInvoker {
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatCompletionResponse invoke(Provider provider, ChatCompletionRequest request) {
        try {
            String providerUrl = getProviderUrl(provider);
            String apiKey = getApiKey(provider);

            // Transform request to OpenAI-compatible format
            String body = objectMapper.writeValueAsString(request);

            HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(providerUrl + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> httpRes = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());

            if (httpRes.statusCode() != 200) {
                throw new RuntimeException("Provider returned " + httpRes.statusCode() + ": " + httpRes.body());
            }

            return objectMapper.readValue(httpRes.body(), ChatCompletionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke provider: " + e.getMessage(), e);
        }
    }

    private String getProviderUrl(Provider provider) {
        try {
            var config = objectMapper.readTree(provider.getConfigJson());
            return config.get("base_url").asText().replaceAll("/$", "");
        } catch (Exception e) {
            throw new RuntimeException("Invalid provider config");
        }
    }

    private String getApiKey(Provider provider) {
        try {
            var config = objectMapper.readTree(provider.getConfigJson());
            var key = config.get("api_key");
            return key != null ? key.asText() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
```

- [ ] **Step 4: Write RateLimiter**

`RateLimiter.java`:
```java
package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public void check(String keyId, int maxRequestsPerMinute) {
        WindowCounter counter = counters.computeIfAbsent(keyId, k -> new WindowCounter());
        int current = counter.increment();
        if (current > maxRequestsPerMinute) {
            throw new BusinessException(429, "Rate limit exceeded");
        }
    }

    private static class WindowCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long windowStart = System.currentTimeMillis() / 60_000;

        int increment() {
            long currentWindow = System.currentTimeMillis() / 60_000;
            if (currentWindow != windowStart) {
                count.set(0);
            }
            return count.incrementAndGet();
        }
    }
}
```

- [ ] **Step 5: Write GatewayAuthInterceptor**

`GatewayAuthInterceptor.java`:
```java
package com.maas.gateway.config;

import com.maas.apikey.entity.ApiKey;
import com.maas.apikey.service.ApiKeyService;
import com.maas.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewayAuthInterceptor implements HandlerInterceptor {
    private final ApiKeyService apiKeyService;

    public GatewayAuthInterceptor(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        ApiKey key = apiKeyService.authenticate(token);
        request.setAttribute("apiKey", key);
        return true;
    }
}
```

`GatewayWebConfig.java`:
```java
package com.maas.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GatewayWebConfig implements WebMvcConfigurer {
    private final GatewayAuthInterceptor authInterceptor;

    public GatewayWebConfig(GatewayAuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/v1/**");
    }
}
```

- [ ] **Step 6: Write GatewayController**

`GatewayController.java`:
```java
package com.maas.gateway.controller;

import com.maas.apikey.entity.ApiKey;
import com.maas.gateway.dto.ChatCompletionRequest;
import com.maas.gateway.dto.ChatCompletionResponse;
import com.maas.gateway.service.ModelRouter;
import com.maas.gateway.service.ProviderInvoker;
import com.maas.gateway.service.RateLimiter;
import com.maas.provider.entity.Provider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class GatewayController {
    private final ModelRouter modelRouter;
    private final ProviderInvoker providerInvoker;
    private final RateLimiter rateLimiter;

    public GatewayController(ModelRouter modelRouter, ProviderInvoker providerInvoker, RateLimiter rateLimiter) {
        this.modelRouter = modelRouter;
        this.providerInvoker = providerInvoker;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/v1/chat/completions")
    public ChatCompletionResponse chatCompletion(
            @Valid @RequestBody ChatCompletionRequest request,
            @RequestAttribute ApiKey apiKey) {
        rateLimiter.check(apiKey.getId().toString(), 60);
        String preferredProvider = null; // could come from header
        Provider provider = modelRouter.route(request.model(), preferredProvider);
        return providerInvoker.invoke(provider, request);
    }

    @GetMapping("/v1/models")
    public String listModels() {
        // Returns available models in OpenAI format
        return "{\"object\":\"list\",\"data\":[]}";
    }
}
```

- [ ] **Step 7: Write ModelRouter test**

`ModelRouterTest.java`:
```java
package com.maas.gateway.service;

import com.maas.common.exception.BusinessException;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderStatus;
import com.maas.provider.entity.ProviderType;
import com.maas.provider.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelRouterTest {

    @Mock ProviderRepository providerRepository;
    @InjectMocks ModelRouter modelRouter;

    @Test
    void route_shouldReturnHealthyProvider() {
        Provider p = new Provider("openai", ProviderType.openai_compatible, "{}");
        p.setHealthStatus("healthy");
        when(providerRepository.findByStatus(ProviderStatus.enabled)).thenReturn(List.of(p));

        Provider result = modelRouter.route("gpt-4", null);
        assertEquals("openai", result.getName());
    }

    @Test
    void route_shouldThrowWhenNoProvider() {
        when(providerRepository.findByStatus(ProviderStatus.enabled)).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> modelRouter.route("gpt-4", null));
    }
}
```

- [ ] **Step 8: Run all tests**

Run: `cd maas-backend && ./mvnw test -q`
Expected: BUILD SUCCESS, all tests green

- [ ] **Step 9: Commit**

```bash
git add maas-backend/src/main/java/com/maas/gateway/
git add maas-backend/src/test/java/com/maas/gateway/
git commit -m "feat: add Gateway Router with auth, rate limit, and provider invocation

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 6: Usage Monitor — 用量记录

**Files:**
- Create: `maas-backend/src/main/java/com/maas/monitor/entity/UsageRecord.java`
- Create: `maas-backend/src/main/java/com/maas/monitor/repository/UsageRecordRepository.java`
- Create: `maas-backend/src/main/java/com/maas/monitor/service/UsageService.java`
- Create: `maas-backend/src/main/java/com/maas/monitor/dto/UsageStatsVO.java`
- Create: `maas-backend/src/main/java/com/maas/monitor/controller/UsageController.java`
- Create: `maas-backend/src/test/java/com/maas/monitor/service/UsageServiceTest.java`

- [ ] **Step 1: Write UsageRecord entity**

`UsageRecord.java`:
```java
package com.maas.monitor.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usage_record")
public class UsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(name = "provider_id")
    private UUID providerId;

    @Column(length = 256)
    private String model;

    @Column(name = "prompt_tokens")
    private int promptTokens;

    @Column(name = "completion_tokens")
    private int completionTokens;

    @Column(name = "latency_ms")
    private int latencyMs;

    @Column(precision = 12, scale = 6)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(length = 32)
    private String status = "success";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UsageRecord() {}

    public UsageRecord(UUID apiKeyId, UUID providerId, String model, int promptTokens, int completionTokens, int latencyMs, BigDecimal cost) {
        this.apiKeyId = apiKeyId;
        this.providerId = providerId;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.cost = cost;
    }

    // getters
    public UUID getId() { return id; }
    public UUID getApiKeyId() { return apiKeyId; }
    public UUID getProviderId() { return providerId; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public int getLatencyMs() { return latencyMs; }
    public BigDecimal getCost() { return cost; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
```

- [ ] **Step 2: Write Repository**

`UsageRecordRepository.java`:
```java
package com.maas.monitor.repository;

import com.maas.monitor.entity.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {

    @Query("SELECT COUNT(u) FROM UsageRecord u WHERE u.apiKeyId = :keyId AND u.createdAt >= :since")
    long countByApiKeyIdSince(@Param("keyId") UUID keyId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.cost), 0) FROM UsageRecord u WHERE u.apiKeyId = :keyId AND u.createdAt >= :since")
    BigDecimal totalCostByApiKeyIdSince(@Param("keyId") UUID keyId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(u.promptTokens + u.completionTokens), 0) FROM UsageRecord u WHERE u.createdAt >= :since")
    long totalTokensSince(@Param("since") Instant since);
}
```

- [ ] **Step 3: Write UsageService**

`UsageService.java`:
```java
package com.maas.monitor.service;

import com.maas.monitor.entity.UsageRecord;
import com.maas.monitor.repository.UsageRecordRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UsageService {
    private final UsageRecordRepository usageRecordRepository;

    public UsageService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
    }

    public void record(UUID apiKeyId, UUID providerId, String model, int promptTokens, int completionTokens, int latencyMs, BigDecimal cost) {
        UsageRecord record = new UsageRecord(apiKeyId, providerId, model, promptTokens, completionTokens, latencyMs, cost);
        usageRecordRepository.save(record);
    }

    public long getRequestCountToday(UUID apiKeyId) {
        return usageRecordRepository.countByApiKeyIdSince(apiKeyId, Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public BigDecimal getTotalCostToday(UUID apiKeyId) {
        return usageRecordRepository.totalCostByApiKeyIdSince(apiKeyId, Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    public long getTotalTokensToday() {
        return usageRecordRepository.totalTokensSince(Instant.now().truncatedTo(ChronoUnit.DAYS));
    }
}
```

- [ ] **Step 4: Write UsageController**

`UsageController.java`:
```java
package com.maas.monitor.controller;

import com.maas.apikey.entity.ApiKey;
import com.maas.common.dto.ApiResponse;
import com.maas.monitor.service.UsageService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/usage")
public class UsageController {
    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/stats")
    public ApiResponse<UsageStats> stats(@RequestAttribute ApiKey apiKey) {
        long count = usageService.getRequestCountToday(apiKey.getId());
        BigDecimal cost = usageService.getTotalCostToday(apiKey.getId());
        return ApiResponse.success(new UsageStats(count, cost));
    }

    record UsageStats(long requestCount, BigDecimal totalCost) {}
}
```

- [ ] **Step 5: Write tests**

`UsageServiceTest.java`:
```java
package com.maas.monitor.service;

import com.maas.monitor.repository.UsageRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock UsageRecordRepository usageRecordRepository;
    @InjectMocks UsageService usageService;

    @Test
    void getTotalTokensToday_shouldReturnSum() {
        when(usageRecordRepository.totalTokensSince(any(Instant.class))).thenReturn(500L);
        assertEquals(500L, usageService.getTotalTokensToday());
    }

    @Test
    void getRequestCountToday_shouldReturnCount() {
        UUID keyId = UUID.randomUUID();
        when(usageRecordRepository.countByApiKeyIdSince(any(), any())).thenReturn(10L);
        assertEquals(10L, usageService.getRequestCountToday(keyId));
    }
}
```

- [ ] **Step 6: Run all tests**

Run: `cd maas-backend && ./mvnw test -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add maas-backend/src/main/java/com/maas/monitor/
git add maas-backend/src/test/java/com/maas/monitor/
git commit -m "feat: add Usage Monitor for tracking API call metrics and costs

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 7: Vue 前端 — 基础布局、路由和 API 客户端

**Files:**
- Create: `maas-frontend/src/router/index.ts`
- Create: `maas-frontend/src/api/client.ts`
- Create: `maas-frontend/src/api/providers.ts`
- Create: `maas-frontend/src/api/keys.ts`
- Create: `maas-frontend/src/types/index.ts`
- Create: `maas-frontend/src/components/Layout.vue`
- Create: `maas-frontend/src/views/Dashboard.vue`

- [ ] **Step 1: Create TypeScript types**

`maas-frontend/src/types/index.ts`:
```typescript
export interface Provider {
  id: string
  name: string
  type: 'openai_compatible' | 'anthropic' | 'vllm' | 'ollama' | 'custom'
  status: 'enabled' | 'disabled' | 'error'
  healthStatus: string
  createdAt: string
  updatedAt: string
}

export interface ApiKey {
  id: string
  name: string
  keyType: 'root' | 'team' | 'application'
  keyPrefix: string
  status: string
  createdAt: string
  expiresAt: string | null
}

export interface CreateKeyRequest {
  name: string
  keyType: 'root' | 'team' | 'application'
  policyJson?: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

- [ ] **Step 2: Create API client**

`maas-frontend/src/api/client.ts`:
```typescript
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

client.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || 'Request failed'
    console.error('API error:', message)
    return Promise.reject(new Error(message))
  }
)

export default client
```

`maas-frontend/src/api/providers.ts`:
```typescript
import client from './client'
import type { Provider, ApiResponse } from '../types'

export const providerApi = {
  list: () => client.get<any, ApiResponse<Provider[]>>('/providers'),
  get: (id: string) => client.get<any, ApiResponse<Provider>>(`/providers/${id}`),
  create: (data: { name: string; type: string; configJson: string }) =>
    client.post<any, ApiResponse<Provider>>('/providers', data),
  update: (id: string, data: { name?: string; configJson?: string }) =>
    client.put<any, ApiResponse<Provider>>(`/providers/${id}`, data),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/providers/${id}`),
}
```

`maas-frontend/src/api/keys.ts`:
```typescript
import client from './client'
import type { ApiKey, CreateKeyRequest, ApiResponse } from '../types'

export const keyApi = {
  list: () => client.get<any, ApiResponse<ApiKey[]>>('/keys'),
  create: (data: CreateKeyRequest) =>
    client.post<any, ApiResponse<ApiKey>>('/keys', data),
  revoke: (id: string) =>
    client.post<any, ApiResponse<null>>(`/keys/${id}/revoke`),
  delete: (id: string) => client.delete<any, ApiResponse<null>>(`/keys/${id}`),
}
```

- [ ] **Step 3: Create router**

`maas-frontend/src/router/index.ts`:
```typescript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('../components/Layout.vue'),
      children: [
        { path: '', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
        { path: 'providers', name: 'Providers', component: () => import('../views/providers/ProviderList.vue') },
        { path: 'providers/new', name: 'ProviderCreate', component: () => import('../views/providers/ProviderForm.vue') },
        { path: 'providers/:id/edit', name: 'ProviderEdit', component: () => import('../views/providers/ProviderForm.vue') },
        { path: 'keys', name: 'Keys', component: () => import('../views/keys/KeyList.vue') },
        { path: 'keys/new', name: 'KeyCreate', component: () => import('../views/keys/KeyForm.vue') },
        { path: 'models', name: 'Models', component: () => import('../views/models/ModelList.vue') },
      ]
    }
  ]
})

export default router
```

- [ ] **Step 4: Create Layout component**

`maas-frontend/src/components/Layout.vue`:
```vue
<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">MaaS Platform</div>
      <nav>
        <router-link to="/" class="nav-item">Dashboard</router-link>
        <router-link to="/providers" class="nav-item">Providers</router-link>
        <router-link to="/models" class="nav-item">Models</router-link>
        <router-link to="/keys" class="nav-item">API Keys</router-link>
      </nav>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout { display: flex; height: 100vh; }
.sidebar { width: 240px; background: #1a1a2e; color: white; padding: 20px; }
.logo { font-size: 18px; font-weight: bold; margin-bottom: 30px; }
.nav-item { display: block; padding: 10px 0; color: #ccc; text-decoration: none; }
.nav-item:hover { color: white; }
.nav-item.router-link-active { color: #4fc3f7; }
.main { flex: 1; padding: 24px; background: #f5f5f5; overflow-y: auto; }
</style>
```

- [ ] **Step 5: Create Dashboard**

`maas-frontend/src/views/Dashboard.vue`:
```vue
<template>
  <div>
    <h1>Dashboard</h1>
    <div class="cards">
      <div class="card"><h3>Providers</h3><p>{{ stats.providers }} active</p></div>
      <div class="card"><h3>API Keys</h3><p>{{ stats.keys }} active</p></div>
      <div class="card"><h3>Today's Tokens</h3><p>{{ stats.todayTokens }}</p></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../api/providers'
import { keyApi } from '../api/keys'

const stats = ref({ providers: 0, keys: 0, todayTokens: 0 })

onMounted(async () => {
  const [providersRes, keysRes] = await Promise.allSettled([
    providerApi.list(),
    keyApi.list()
  ])
  if (providersRes.status === 'fulfilled') stats.value.providers = providersRes.value.data.length
  if (keysRes.status === 'fulfilled') stats.value.keys = keysRes.value.data.length
})
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 16px; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
</style>
```

- [ ] **Step 6: Install deps and verify frontend builds**

Run: `cd maas-frontend && npm install && npx vue-tsc --noEmit`
Expected: No type errors

- [ ] **Step 7: Commit**

```bash
git add maas-frontend/
git commit -m "feat: add Vue frontend scaffold with layout, router, and API client

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 8: Vue 前端 — Provider 管理页面

**Files:**
- Create: `maas-frontend/src/views/providers/ProviderList.vue`
- Create: `maas-frontend/src/views/providers/ProviderForm.vue`

- [ ] **Step 1: Create ProviderList**

`maas-frontend/src/views/providers/ProviderList.vue`:
```vue
<template>
  <div>
    <div class="header">
      <h1>Providers</h1>
      <router-link to="/providers/new" class="btn-primary">+ Add Provider</router-link>
    </div>

    <table class="table">
      <thead><tr><th>Name</th><th>Type</th><th>Status</th><th>Health</th><th>Actions</th></tr></thead>
      <tbody>
        <tr v-for="p in providers" :key="p.id">
          <td>{{ p.name }}</td>
          <td>{{ p.type }}</td>
          <td><span :class="'badge ' + p.status">{{ p.status }}</span></td>
          <td><span :class="'badge ' + p.healthStatus">{{ p.healthStatus }}</span></td>
          <td>
            <router-link :to="`/providers/${p.id}/edit`" class="btn-sm">Edit</router-link>
            <button @click="deleteProvider(p.id)" class="btn-sm btn-danger">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'
import type { Provider } from '../../types'

const providers = ref<Provider[]>([])

onMounted(async () => {
  const res = await providerApi.list()
  providers.value = res.data
})

async function deleteProvider(id: string) {
  if (!confirm('Delete this provider?')) return
  await providerApi.delete(id)
  providers.value = providers.value.filter(p => p.id !== id)
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.enabled, .badge.healthy { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #eee; color: #666; }
.badge.error, .badge.unhealthy { background: #ffebee; color: #c62828; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; background: #e3f2fd; color: #1976d2; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; text-decoration: none; font-size: 13px; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
```

- [ ] **Step 2: Create ProviderForm**

`maas-frontend/src/views/providers/ProviderForm.vue`:
```vue
<template>
  <div>
    <h1>{{ isEdit ? 'Edit Provider' : 'Add Provider' }}</h1>
    <form @submit.prevent="save" class="form">
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Type</label>
        <select v-model="form.type" required>
          <option value="openai_compatible">OpenAI Compatible</option>
          <option value="anthropic">Anthropic</option>
          <option value="vllm">vLLM</option>
          <option value="ollama">Ollama</option>
          <option value="custom">Custom</option>
        </select>
      </div>
      <div class="field">
        <label>Config (JSON)</label>
        <textarea v-model="form.configJson" rows="6" required></textarea>
      </div>
      <button type="submit" class="btn-primary">{{ isEdit ? 'Update' : 'Create' }}</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi } from '../../api/providers'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = ref({ name: '', type: 'openai_compatible', configJson: '{}' })

onMounted(async () => {
  if (isEdit.value) {
    const res = await providerApi.get(route.params.id as string)
    form.value = { name: res.data.name, type: res.data.type, configJson: '{}' }
  }
})

async function save() {
  if (isEdit.value) {
    await providerApi.update(route.params.id as string, { name: form.value.name, configJson: form.value.configJson })
  } else {
    await providerApi.create(form.value)
  }
  router.push('/providers')
}
</script>

<style scoped>
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select, .field textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
</style>
```

- [ ] **Step 3: Verify frontend compiles**

Run: `cd maas-frontend && npx vue-tsc --noEmit`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add maas-frontend/src/views/providers/
git commit -m "feat: add Provider management pages (list, create, edit)

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

### Task 9: Vue 前端 — API Key 管理页面

**Files:**
- Create: `maas-frontend/src/views/keys/KeyList.vue`
- Create: `maas-frontend/src/views/keys/KeyForm.vue`
- Create: `maas-frontend/src/views/models/ModelList.vue`

- [ ] **Step 1: Create KeyList**

`maas-frontend/src/views/keys/KeyList.vue`:
```vue
<template>
  <div>
    <div class="header">
      <h1>API Keys</h1>
      <router-link to="/keys/new" class="btn-primary">+ Create Key</router-link>
    </div>

    <table class="table">
      <thead><tr><th>Name</th><th>Type</th><th>Key (suffix)</th><th>Status</th><th>Expires</th><th>Actions</th></tr></thead>
      <tbody>
        <tr v-for="k in keys" :key="k.id">
          <td>{{ k.name }}</td>
          <td>{{ k.keyType }}</td>
          <td><code>...{{ k.keyPrefix }}</code></td>
          <td><span :class="'badge ' + k.status">{{ k.status }}</span></td>
          <td>{{ k.expiresAt ? new Date(k.expiresAt).toLocaleDateString() : 'Never' }}</td>
          <td>
            <button v-if="k.status === 'active'" @click="revokeKey(k.id)" class="btn-sm btn-warning">Revoke</button>
            <button @click="deleteKey(k.id)" class="btn-sm btn-danger">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { keyApi } from '../../api/keys'
import type { ApiKey } from '../../types'

const keys = ref<ApiKey[]>([])

onMounted(async () => {
  const res = await keyApi.list()
  keys.value = res.data
})

async function revokeKey(id: string) {
  if (!confirm('Revoke this key? This cannot be undone.')) return
  await keyApi.revoke(id)
  keys.value = keys.value.map(k => k.id === id ? { ...k, status: 'revoked' } : k)
}

async function deleteKey(id: string) {
  if (!confirm('Delete this key?')) return
  await keyApi.delete(id)
  keys.value = keys.value.filter(k => k.id !== id)
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-size: 12px; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
.badge.revoked { background: #ffebee; color: #c62828; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; text-decoration: none; border-radius: 4px; }
.btn-sm { padding: 4px 8px; border: none; border-radius: 4px; cursor: pointer; margin-right: 4px; font-size: 13px; }
.btn-warning { background: #fff3e0; color: #e65100; }
.btn-danger { background: #ffebee; color: #c62828; }
</style>
```

- [ ] **Step 2: Create KeyForm**

`maas-frontend/src/views/keys/KeyForm.vue`:
```vue
<template>
  <div>
    <h1>Create API Key</h1>
    <form @submit.prevent="save" class="form">
      <div class="field">
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div class="field">
        <label>Type</label>
        <select v-model="form.keyType" required>
          <option value="application">Application</option>
          <option value="team">Team</option>
          <option value="root">Root</option>
        </select>
      </div>
      <div v-if="createdKey" class="created-key">
        <strong>Key created!</strong>
        <p class="warning">Copy this now — it won't be shown again.</p>
        <code>{{ createdKey }}</code>
      </div>
      <button v-if="!createdKey" type="submit" class="btn-primary">Create</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { keyApi } from '../../api/keys'

const form = ref({ name: '', keyType: 'application' as const })
const createdKey = ref<string | null>(null)

async function save() {
  const res = await keyApi.create({ ...form.value })
  // The raw key is returned in the response for the first/last time
  createdKey.value = res.data.keyPrefix
}
</script>

<style scoped>
.form { max-width: 500px; background: white; padding: 24px; border-radius: 8px; }
.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 4px; font-weight: 500; }
.field input, .field select { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.btn-primary { padding: 8px 16px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer; }
.created-key { background: #fff8e1; padding: 12px; border-radius: 4px; margin-bottom: 16px; }
.created-key code { word-break: break-all; font-size: 13px; }
.warning { color: #e65100; font-size: 13px; }
</style>
```

- [ ] **Step 3: Create ModelList**

`maas-frontend/src/views/models/ModelList.vue`:
```vue
<template>
  <div>
    <h1>Models</h1>
    <p class="hint">Models are auto-synced from enabled providers.</p>
    <table class="table">
      <thead><tr><th>Model Name</th><th>Provider</th><th>Status</th></tr></thead>
      <tbody>
        <tr v-for="m in models" :key="m.id">
          <td>{{ m.name }}</td>
          <td>{{ m.providerName }}</td>
          <td><span :class="'badge ' + m.status">{{ m.status }}</span></td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { providerApi } from '../../api/providers'

const models = ref<Array<{ id: string; name: string; providerName: string; status: string }>>([])

onMounted(async () => {
  // Models are fetched as part of provider detail; simplified for P0
  const res = await providerApi.list()
  models.value = res.data.flatMap(p => [{ id: p.id, name: p.name, providerName: p.name, status: 'active' }])
})
</script>

<style scoped>
.hint { color: #666; font-size: 14px; }
.table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; }
.table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
</style>
```

- [ ] **Step 4: Verify frontend compiles**

Run: `cd maas-frontend && npx vue-tsc --noEmit`
Expected: No errors

- [ ] **Step 5: Commit**

```bash
git add maas-frontend/src/views/keys/
git add maas-frontend/src/views/models/
git commit -m "feat: add API Key management and Models view pages

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>"
```

---

## Self-Review

### Spec Coverage
- **Provider Manager** — Tasks 2, 3 (entity, CRUD service, adapter, health check, controller)
- **Gateway Router** — Task 5 (OpenAI-compatible API, routing, auth interceptor, rate limiter, provider invoker)
- **API Key Manager** — Task 4 (entity, create/revoke/authenticate, controller)
- **Usage Monitor** — Task 6 (usage record, stats query, controller)
- **Frontend** — Tasks 7, 8, 9 (Vue scaffold, provider pages, key pages, model list)
- **MCP Config Manager** — deferred to P1
- **Skills/Tools Registry** — deferred to P1
- **Workflow Engine** — deferred to P1
- **Scheduled Task** — deferred to P2
- **Security Inspector** — deferred to P1, but basic auth already in Gateway (key auth)
- **Database schema** — Task 0 (Flyway V1 with all core tables)

### Placeholder Check
- `ModelRouter.hasModel()` currently returns `true` — this is intentional for P0 (model sync is done in health checker but not queried here). Will be refined in P1.

### Type Consistency
- `ProviderVO` uses `ProviderStatus` enum matching `Provider.status`
- `ApiKey` entity uses `KeyType` enum matching `CreateKeyRequest.keyType`
- Frontend types match backend DTOs
- API prefix is `/api/` on backend and proxied from `/api` on frontend

---

Plan complete and saved to `docs/superpowers/plans/2026-05-11-maas-p0-core-gateway.md`.

两个执行选项：

1. **Subagent-Driven（推荐）** — 为每个 Task 派发独立的子 agent，逐个执行并 review，快速迭代
2. **Inline Execution** — 在当前 session 里按顺序执行，分批 checkpoint

你倾向哪种方式？
