package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;

public final class OfferingMapper {

    private OfferingMapper() {
    }

    public static OfferingJpa toJpa(Offering offering, ProviderJpa provider) {
        return new OfferingJpa(
                offering.getId(),
                provider,
                offering.getName().value(),
                offering.getName().normalized(),
                offering.getDescription(),
                offering.getConditions(),
                offering.getPrice().value(),
                offering.getCategory(),
                offering.getType(),
                offering.getImageUrl()
        );
    }

    public static Offering toDomain(OfferingJpa entity) {
        return new Offering(
                entity.getId(),
                entity.getProvider().getId(),
                new OfferingName(entity.getName()),
                entity.getDescription(),
                entity.getConditions(),
                new Price(entity.getPrice()),
                entity.getCategory(),
                entity.getType(),
                entity.getImageUrl()
        );
    }
}
