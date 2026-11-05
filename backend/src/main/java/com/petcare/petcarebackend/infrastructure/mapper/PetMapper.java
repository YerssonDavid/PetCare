package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;

public final class PetMapper {

    private PetMapper() {
    }

    public static PetJpa toJpa(Pet pet, OwnerJpa owner) {
        return new PetJpa(
                pet.getId(),
                owner,
                pet.getName().value(),
                pet.getName().normalized(),
                pet.getTypePet(),
                pet.getAge(),
                pet.getDescription()
        );
    }

    public static Pet toDomain(PetJpa entity) {
        return new Pet(
                entity.getId(),
                entity.getOwner().getId(),
                new PetName(entity.getName()),
                entity.getTypePet(),
                entity.getAge(),
                entity.getDescription()
        );
    }
}
