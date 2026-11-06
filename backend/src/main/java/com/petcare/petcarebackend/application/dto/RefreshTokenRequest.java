package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank
        String refreshToken
) {
}
