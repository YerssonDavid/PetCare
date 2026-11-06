package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;

import java.util.UUID;

public interface ServiceRequestRegistry {
    ServiceRequestResponse createServiceRequest(UUID ownerId, CreateServiceRequestRequest request);
}
