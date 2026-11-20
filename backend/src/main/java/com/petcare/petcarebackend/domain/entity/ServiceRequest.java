package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.PurchaseType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
import com.petcare.petcarebackend.domain.valueobject.Price;

import java.time.LocalDateTime;
import java.util.UUID;

public class ServiceRequest {
    private UUID id;
    private UUID ownerId;
    private UUID petId;
    private UUID offeringId;
    private UUID providerId;
    private UUID slotId;
    private Price price;
    private RequestStatus status;
    private PurchaseType purchaseType;
    private String rejectionReason;
    private LocalDateTime requestedAt;

    public ServiceRequest() {
    }

    public ServiceRequest(UUID id, UUID ownerId, UUID petId, UUID offeringId, UUID providerId, UUID slotId,
                          Price price, RequestStatus status, PurchaseType purchaseType,
                          String rejectionReason, LocalDateTime requestedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.petId = petId;
        this.offeringId = offeringId;
        this.providerId = providerId;
        this.slotId = slotId;
        this.price = price;
        this.status = status;
        this.purchaseType = purchaseType;
        this.rejectionReason = rejectionReason;
        this.requestedAt = requestedAt;
    }

    // Create factory; the price is copied so later changes to the offering do not alter this request
    public static ServiceRequest create(UUID ownerId, UUID petId, Offering offering, TimeSlot slot,
                                        PurchaseType purchaseType, LocalDateTime now) {
        if (offering.getType() == OfferingType.PRODUCTO && purchaseType == null) {
            throw new IllegalArgumentException("El tipo de compra es obligatorio para los productos");
        }
        if (offering.getType() == OfferingType.SERVICIO && purchaseType != null) {
            throw new IllegalArgumentException("El tipo de compra solo aplica a los productos");
        }
        return new ServiceRequest(UUID.randomUUID(), ownerId, petId, offering.getId(), offering.getProviderId(),
                slot.getId(), offering.getPrice(), RequestStatus.PENDIENTE, purchaseType, null, now);
    }

    public void accept() {
        ensurePending();
        this.status = RequestStatus.ACEPTADO;
    }

    public void reject(String reason) {
        ensurePending();
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
        }
        this.status = RequestStatus.RECHAZADO;
        this.rejectionReason = reason.trim();
    }

    public boolean involves(UUID accountId) {
        return ownerId.equals(accountId) || providerId.equals(accountId);
    }

    private void ensurePending() {
        if (status != RequestStatus.PENDIENTE) {
            throw new RequestNotPendingException();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public UUID getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(UUID offeringId) {
        this.offeringId = offeringId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public UUID getSlotId() {
        return slotId;
    }

    public void setSlotId(UUID slotId) {
        this.slotId = slotId;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public PurchaseType getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(PurchaseType purchaseType) {
        this.purchaseType = purchaseType;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
