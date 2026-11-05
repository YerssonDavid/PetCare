package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ServiceRequestHistoryResponse(
        UUID requestId,
        String offeringName,
        String offeringDescription,
        OfferingType offeringType,
        PetResponse pet,
        List<Entry> entries
) {
    public record Entry(RequestStatus status, String reason, LocalDateTime changedAt) {
        static Entry from(ServiceRequestHistory history) {
            return new Entry(history.getStatus(), history.getReason(), history.getChangedAt());
        }
    }

    public static ServiceRequestHistoryResponse from(ServiceRequestDetails details, List<ServiceRequestHistory> history) {
        Offering offering = details.offering();
        return new ServiceRequestHistoryResponse(
                details.request().getId(),
                offering.getName().value(),
                offering.getDescription(),
                offering.getType(),
                PetResponse.from(details.pet()),
                history.stream().map(Entry::from).toList()
        );
    }
}
