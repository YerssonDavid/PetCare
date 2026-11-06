package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.PurchaseType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerJpa owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetJpa pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id", nullable = false)
    private OfferingJpa offering;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderJpa provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlotJpa slot;

    // Copy of the offering price at request time
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    // Only set for products
    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_type", length = 20)
    private PurchaseType purchaseType;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;
}
