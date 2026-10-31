package com.petcare.petcarebackend.application.port.out;

import com.petcare.petcarebackend.domain.entity.Provider;

public interface ProviderRepositoryPort {
    void saveProvider(Provider provider);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}
