package com.petcare.petcarebackend.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterPetResponse(
        UUID id,
        String message,
        LocalDateTime timestamp
) {
}
