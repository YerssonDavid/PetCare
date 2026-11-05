package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.TypeProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RegisterProviderRequest(
        @NotBlank
        @Size(max = 100)
        String nameCommercial,

        @NotNull
        TypeProvider typeProvider,

        @NotBlank
        @Email
        @Size(max = 150)
        String email,

        @NotBlank
        // Separators allowed here; Phone value object strips them and enforces 7-15 digits
        @Pattern(regexp = "^\\+?[0-9\\s()-]{7,25}$")
        String phone,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        @Size(max = 100)
        String city,

        // Optional: independent caregivers may not have a public address
        @Size(max = 200)
        String address,

        @NotNull
        @PositiveOrZero
        @Max(80)
        Integer experienceYears,

        @Size(max = 500)
        String description
) {
}
