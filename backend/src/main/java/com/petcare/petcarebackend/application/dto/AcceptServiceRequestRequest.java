package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AcceptServiceRequestRequest(
        // TODO: remove once JWT exists; the provider ID must come from the token
        @NotNull
        UUID providerId
) {
}
