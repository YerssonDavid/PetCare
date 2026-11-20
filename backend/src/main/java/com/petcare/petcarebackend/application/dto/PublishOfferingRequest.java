package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PublishOfferingRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Size(max = 500)
        String description,

        @Size(max = 500)
        String conditions,

        @NotNull
        @PositiveOrZero
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotNull
        OfferingCategory category,

        @NotNull
        OfferingType type
) {
}
