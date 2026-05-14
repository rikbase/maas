package com.maas.provider.controller;

import com.maas.common.dto.ApiResponse;
import com.maas.provider.dto.ModelCreateRequest;
import com.maas.provider.dto.ModelUpdateRequest;
import com.maas.provider.dto.ModelVO;
import com.maas.provider.entity.Provider;
import com.maas.provider.entity.ProviderModel;
import com.maas.provider.repository.ProviderModelRepository;
import com.maas.provider.repository.ProviderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ProviderModelRepository modelRepository;
    private final ProviderRepository providerRepository;

    public ModelController(ProviderModelRepository modelRepository, ProviderRepository providerRepository) {
        this.modelRepository = modelRepository;
        this.providerRepository = providerRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<List<ModelVO>> list() {
        List<ProviderModel> models = modelRepository.findAll();
        return ApiResponse.success(models.stream()
            .map(m -> ModelVO.from(m, m.getProvider().getName()))
            .toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ApiResponse<ModelVO> get(@PathVariable UUID id) {
        ProviderModel m = modelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Model not found"));
        return ApiResponse.success(ModelVO.from(m, m.getProvider().getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ModelVO> create(@Valid @RequestBody ModelCreateRequest req) {
        Provider provider = providerRepository.findById(req.providerId())
            .orElseThrow(() -> new RuntimeException("Provider not found"));
        ProviderModel m = new ProviderModel(provider, req.modelName());
        if (req.capabilities() != null) m.setCapabilities(req.capabilities());
        if (req.status() != null) m.setStatus(req.status());
        m = modelRepository.save(m);
        return ApiResponse.success(ModelVO.from(m, provider.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<ModelVO> update(@PathVariable UUID id, @Valid @RequestBody ModelUpdateRequest req) {
        ProviderModel m = modelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Model not found"));
        if (req.modelName() != null) m.setModelName(req.modelName());
        if (req.capabilities() != null) m.setCapabilities(req.capabilities());
        if (req.status() != null) m.setStatus(req.status());
        m = modelRepository.save(m);
        return ApiResponse.success(ModelVO.from(m, m.getProvider().getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        modelRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}
