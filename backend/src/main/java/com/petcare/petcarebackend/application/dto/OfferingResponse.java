package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;

import java.math.BigDecimal;
import java.util.UUID;

public record OfferingResponse(
        UUID id,
        UUID providerId,
        String name,
        String description,
        String conditions,
        BigDecimal price,
        OfferingCategory category,
        OfferingType type,
        String imageUrl
) {
    public static OfferingResponse from(Offering offering) {
        return new OfferingResponse(
                offering.getId(),
                offering.getProviderId(),
                offering.getName().value(),
                offering.getDescription(),
                offering.getConditions(),
                offering.getPrice().value(),
                offering.getCategory(),
                offering.getType(),
                offering.getImageUrl()
        );
    }
}
