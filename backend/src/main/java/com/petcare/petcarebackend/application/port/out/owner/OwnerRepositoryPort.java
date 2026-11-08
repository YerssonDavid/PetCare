package com.petcare.petcarebackend.application.port.out.owner;

import com.petcare.petcarebackend.domain.entity.Owner;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepositoryPort {
    void saveOwner(Owner owner);
    boolean existsByEmail(String email);
    boolean existsById(UUID id);
    Optional<Owner> findById(UUID id);
    Optional<Owner> findByEmail(String email);
}
