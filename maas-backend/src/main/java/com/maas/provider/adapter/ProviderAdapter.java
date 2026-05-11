package com.maas.provider.adapter;

import com.maas.provider.entity.Provider;
import java.util.List;

public interface ProviderAdapter {
    boolean supports(Provider provider);
    boolean checkHealth(Provider provider);
    List<String> fetchModels(Provider provider);
}
