package com.petcare.petcarebackend.application.dto;

import java.time.LocalDateTime;

public record ResponseHttp(
        String message,
        boolean success,
        LocalDateTime timestamp
) {
}
