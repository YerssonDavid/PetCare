package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.RejectServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;

import java.util.UUID;

public interface ServiceRequestAnswerRegistry {
    ServiceRequestResponse acceptServiceRequest(UUID requestId, UUID providerId);
    ServiceRequestResponse rejectServiceRequest(UUID requestId, UUID providerId, RejectServiceRequestRequest request);
}
