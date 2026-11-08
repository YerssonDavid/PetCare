package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.ServiceRequestHistoryResponse;

import java.util.UUID;

public interface ServiceRequestHistoryQuery {
    ServiceRequestHistoryResponse getServiceRequestHistory(UUID requestId, UUID accountId);
}
