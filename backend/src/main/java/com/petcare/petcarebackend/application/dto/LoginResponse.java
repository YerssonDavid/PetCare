package com.petcare.petcarebackend.application.dto;

public record LoginResponse(
        String token,
        String refreshToken
) {
}
