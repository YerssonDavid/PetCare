package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.PurchaseType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ServiceRequestResponse(
        UUID id,
        String petName,
        String offeringName,
        OfferingType offeringType,
        String providerName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal price,
        RequestStatus status,
        PurchaseType purchaseType,
        String rejectionReason,
        LocalDateTime requestedAt
) {
    public static ServiceRequestResponse from(ServiceRequest request, Pet pet, Offering offering,
                                              Provider provider, TimeSlot slot) {
        return new ServiceRequestResponse(
                request.getId(),
                pet.getName().value(),
                offering.getName().value(),
                offering.getType(),
                provider.getNameCommercial().value(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                request.getPrice().value(),
                request.getStatus(),
                request.getPurchaseType(),
                request.getRejectionReason(),
                request.getRequestedAt()
        );
    }
}
