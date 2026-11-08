package com.petcare.petcarebackend.application.dto;

import java.util.UUID;

public record LoginOwnerResponse(UUID id, String name, String email) {
}
