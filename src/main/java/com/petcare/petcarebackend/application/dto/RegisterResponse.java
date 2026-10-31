package com.petcare.petcarebackend.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// Shared response for every registration use case (owner, provider, pet)
public record RegisterResponse(
        UUID id,
        String message,
        LocalDateTime timestamp
) {
}
