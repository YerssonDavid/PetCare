package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.valueobject.Price;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;

public final class ServiceRequestMapper {

    private ServiceRequestMapper() {
    }

    public static ServiceRequestJpa toJpa(ServiceRequest request, OwnerJpa owner, PetJpa pet, OfferingJpa offering,
                                          ProviderJpa provider, TimeSlotJpa slot) {
        return new ServiceRequestJpa(
                request.getId(),
                owner,
                pet,
                offering,
                provider,
                slot,
                request.getPrice().value(),
                request.getStatus(),
                request.getPurchaseType(),
                request.getRejectionReason(),
                request.getRequestedAt()
        );
    }

    public static ServiceRequest toDomain(ServiceRequestJpa entity) {
        return new ServiceRequest(
                entity.getId(),
                entity.getOwner().getId(),
                entity.getPet().getId(),
                entity.getOffering().getId(),
                entity.getProvider().getId(),
                entity.getSlot().getId(),
                new Price(entity.getPrice()),
                entity.getStatus(),
                entity.getPurchaseType(),
                entity.getRejectionReason(),
                entity.getRequestedAt()
        );
    }
}
