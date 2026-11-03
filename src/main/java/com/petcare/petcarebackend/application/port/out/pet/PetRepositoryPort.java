package com.petcare.petcarebackend.application.port.out.pet;

import com.petcare.petcarebackend.domain.entity.Pet;

import java.util.List;
import java.util.UUID;

public interface PetRepositoryPort {
    void savePet(Pet pet);
    boolean existsByOwnerIdAndNormalizedName(UUID ownerId, String normalizedName);
    List<Pet> findPetsByIdOwner(UUID ownerId);
}
