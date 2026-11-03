package com.petcare.petcarebackend.application.port.out.request;

import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;

public interface ServiceRequestHistoryRepositoryPort {
    void saveHistory(ServiceRequestHistory history);
}
