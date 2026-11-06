package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_request_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestHistoryJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ServiceRequestJpa request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerJpa owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
