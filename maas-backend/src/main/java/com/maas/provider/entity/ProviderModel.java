package com.maas.provider.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    @JdbcTypeCode(SqlTypes.JSON)
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
