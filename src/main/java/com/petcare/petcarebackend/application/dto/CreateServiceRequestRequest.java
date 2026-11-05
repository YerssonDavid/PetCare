package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.PurchaseType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateServiceRequestRequest(
        @NotNull
        UUID petId,

        @NotNull
        UUID slotId,

        // Required for products, not allowed for services
        PurchaseType purchaseType
) {
}
