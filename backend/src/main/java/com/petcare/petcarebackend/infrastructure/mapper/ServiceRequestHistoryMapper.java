package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestHistoryJpa;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;

public final class ServiceRequestHistoryMapper {

    private ServiceRequestHistoryMapper() {
    }

    public static ServiceRequestHistoryJpa toJpa(ServiceRequestHistory history, ServiceRequestJpa request, OwnerJpa owner) {
        return new ServiceRequestHistoryJpa(
                history.getId(),
                request,
                owner,
                history.getStatus(),
                history.getReason(),
                history.getChangedAt()
        );
    }

    public static ServiceRequestHistory toDomain(ServiceRequestHistoryJpa entity) {
        return new ServiceRequestHistory(
                entity.getId(),
                entity.getRequest().getId(),
                entity.getOwner().getId(),
                entity.getStatus(),
                entity.getReason(),
                entity.getChangedAt()
        );
    }
}
