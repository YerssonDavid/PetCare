package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RejectServiceRequestRequest(
        // TODO: remove once JWT exists; the provider ID must come from the token
        @NotNull
        UUID providerId,

        @NotBlank
        @Size(max = 500)
        String reason
) {
}
