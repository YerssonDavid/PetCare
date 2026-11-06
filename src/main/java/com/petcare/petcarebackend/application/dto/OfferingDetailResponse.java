package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OfferingDetailResponse(
        UUID id,
        String name,
        String description,
        String conditions,
        BigDecimal price,
        OfferingCategory category,
        OfferingType type,
        String imageUrl,
        UUID providerId,
        String providerName,
        String providerPhone,
        List<TimeSlotResponse> freeSlots
) {
    public static OfferingDetailResponse from(Offering offering, Provider provider, List<TimeSlot> freeSlots) {
        return new OfferingDetailResponse(
                offering.getId(),
                offering.getName().value(),
                offering.getDescription(),
                offering.getConditions(),
                offering.getPrice().value(),
                offering.getCategory(),
                offering.getType(),
                offering.getImageUrl(),
                provider.getId(),
                provider.getNameCommercial().value(),
                provider.getPhone().value(),
                freeSlots.stream().map(TimeSlotResponse::from).toList()
        );
    }
}
