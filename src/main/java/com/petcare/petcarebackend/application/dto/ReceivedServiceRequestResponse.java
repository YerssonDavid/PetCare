package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
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

// What a provider sees of a request: of the client, only the pet and the email
public record ReceivedServiceRequestResponse(
        UUID id,
        PetResponse pet,
        String ownerEmail,
        String offeringName,
        OfferingType offeringType,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal price,
        RequestStatus status,
        PurchaseType purchaseType,
        String rejectionReason,
        LocalDateTime requestedAt
) {
    public static ReceivedServiceRequestResponse from(ServiceRequestDetails details) {
        ServiceRequest request = details.request();
        Offering offering = details.offering();
        TimeSlot slot = details.slot();
        return new ReceivedServiceRequestResponse(
                request.getId(),
                PetResponse.from(details.pet()),
                details.owner().getEmail().value(),
                offering.getName().value(),
                offering.getType(),
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
