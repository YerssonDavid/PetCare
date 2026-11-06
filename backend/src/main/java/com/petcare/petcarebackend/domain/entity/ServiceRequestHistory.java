package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

// One entry per status change of a request
public class ServiceRequestHistory {
    private UUID id;
    private UUID requestId;
    private UUID ownerId;
    private RequestStatus status;
    private String reason;
    private LocalDateTime changedAt;

    public ServiceRequestHistory() {
    }

    public ServiceRequestHistory(UUID id, UUID requestId, UUID ownerId, RequestStatus status,
                                 String reason, LocalDateTime changedAt) {
        this.id = id;
        this.requestId = requestId;
        this.ownerId = ownerId;
        this.status = status;
        this.reason = reason;
        this.changedAt = changedAt;
    }

    // Create factory: captures the current state of the request
    public static ServiceRequestHistory of(ServiceRequest request, LocalDateTime changedAt) {
        return new ServiceRequestHistory(UUID.randomUUID(), request.getId(), request.getOwnerId(),
                request.getStatus(), request.getRejectionReason(), changedAt);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
