package com.petcare.petcarebackend.application.port.out.provider;

import com.petcare.petcarebackend.domain.entity.Provider;

import java.util.Optional;
import java.util.UUID;

public interface ProviderRepositoryPort {
    void saveProvider(Provider provider);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<Provider> findById(UUID id);
    // Holds a row lock until the surrounding transaction ends; requires an active transaction
    Optional<Provider> findByIdForUpdate(UUID id);
}
