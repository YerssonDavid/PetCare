package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.Role;

import java.util.UUID;

public record ClaimsUser(
        UUID id,
        Role role
) {
}
