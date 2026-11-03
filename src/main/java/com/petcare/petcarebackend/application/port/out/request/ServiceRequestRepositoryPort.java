package com.petcare.petcarebackend.application.port.out.request;

import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.enums.RequestStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestRepositoryPort {
    void saveServiceRequest(ServiceRequest request);
    // Only requests for services count: products never block a pet's schedule
    boolean existsActiveServiceRequestOverlapping(UUID petId, LocalDate date, LocalTime startTime, LocalTime endTime);
    boolean existsActiveRequestForPetAndSlot(UUID petId, UUID slotId);
    Optional<ServiceRequestDetails> findDetailsById(UUID id);
    // Most recent first; a null status returns the requests in every status
    List<ServiceRequestDetails> findDetailsByProvider(UUID providerId, RequestStatus status);
    // Atomic: stores the request's status and reason, and returns false if it was already answered,
    // so only one concurrent answer can win
    boolean updateIfPending(ServiceRequest request);
}
