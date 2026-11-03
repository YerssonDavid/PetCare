package com.petcare.petcarebackend.application.port.out.request;

import com.petcare.petcarebackend.domain.entity.ServiceRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface ServiceRequestRepositoryPort {
    void saveServiceRequest(ServiceRequest request);
    // Only requests for services count: products never block a pet's schedule
    boolean existsActiveServiceRequestOverlapping(UUID petId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
