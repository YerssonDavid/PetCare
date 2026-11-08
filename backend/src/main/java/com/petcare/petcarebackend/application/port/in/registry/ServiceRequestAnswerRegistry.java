package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.AcceptServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.RejectServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;

import java.util.UUID;

public interface ServiceRequestAnswerRegistry {
    ServiceRequestResponse acceptServiceRequest(UUID requestId, AcceptServiceRequestRequest request);
    ServiceRequestResponse rejectServiceRequest(UUID requestId, RejectServiceRequestRequest request);
    ServiceRequestResponse cancelServiceRequest(UUID requestId, UUID ownerId);
}
