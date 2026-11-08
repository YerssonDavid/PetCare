package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.PurchaseType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateServiceRequestRequest(
        // TODO: remove once JWT exists; the owner ID must come from the token
        @NotNull
        UUID ownerId,

        @NotNull
        UUID petId,

        @NotNull
        UUID slotId,

        // Required for products, not allowed for services
        PurchaseType purchaseType
) {
}
