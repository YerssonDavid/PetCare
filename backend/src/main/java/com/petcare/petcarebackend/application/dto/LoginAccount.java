package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.Role;

import java.util.UUID;

// What the login needs from an owner or a provider. Holds the password hash: never return it in a response
public record LoginAccount(
        UUID id,
        String passwordHash,
        Role role,
        boolean active
) {
}
