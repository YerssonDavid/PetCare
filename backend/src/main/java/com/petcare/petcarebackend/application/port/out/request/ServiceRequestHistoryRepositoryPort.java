package com.petcare.petcarebackend.application.port.out.request;

import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;

import java.util.List;
import java.util.UUID;

public interface ServiceRequestHistoryRepositoryPort {
    void saveHistory(ServiceRequestHistory history);
    // Most recent first
    List<ServiceRequestHistory> findByRequestId(UUID requestId);
}
