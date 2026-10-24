package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterOwnerRequest(
        @NotBlank
        @Size(max = 100)
        String name,

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
        String password
) {
}
