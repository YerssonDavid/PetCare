package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;

public interface ServiceRequestRegistry {
    ServiceRequestResponse createServiceRequest(CreateServiceRequestRequest request);
}
