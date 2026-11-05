package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.TypePet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterPetRequest(
        // TODO: remove once JWT exists; the owner ID must come from the token
        @NotNull
        UUID ownerId,

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        TypePet typePet,

        @NotNull
        @PositiveOrZero
        Integer age,

        @Size(max = 500)
        String description
) {
}
