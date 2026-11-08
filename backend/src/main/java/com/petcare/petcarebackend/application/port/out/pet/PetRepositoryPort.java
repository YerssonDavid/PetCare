package com.petcare.petcarebackend.application.port.out.pet;

import com.petcare.petcarebackend.domain.entity.Pet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepositoryPort {
    void savePet(Pet pet);
    boolean existsByOwnerIdAndNormalizedName(UUID ownerId, String normalizedName);
    boolean deleteByIdAndOwnerId(UUID petId, UUID ownerId);
    List<Pet> findPetsByIdOwner(UUID ownerId);
    // Holds a row lock until the surrounding transaction ends; requires an active transaction
    Optional<Pet> findByIdForUpdate(UUID id);
}
