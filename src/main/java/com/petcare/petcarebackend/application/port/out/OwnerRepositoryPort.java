package com.petcare.petcarebackend.application.port.out;

import com.petcare.petcarebackend.domain.entity.Owner;

import java.util.UUID;

public interface OwnerRepositoryPort {
    void saveOwner(Owner owner);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
}
