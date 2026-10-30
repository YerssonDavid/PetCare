package com.petcare.petcarebackend.application.port.out;

import com.petcare.petcarebackend.domain.entity.Pet;

import java.util.UUID;

public interface PetRepositoryPort {
    void savePet(Pet pet);
    boolean existsByOwnerIdAndNormalizedName(UUID ownerId, String normalizedName);
}
