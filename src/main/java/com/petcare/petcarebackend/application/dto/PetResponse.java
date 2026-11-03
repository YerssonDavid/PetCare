package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.enums.TypePet;

import java.util.UUID;

public record PetResponse(
        UUID id,
        String name,
        TypePet typePet,
        int age,
        String description
) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName().value(),
                pet.getTypePet(),
                pet.getAge(),
                pet.getDescription()
        );
    }
}
