package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestHistoryJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRequestHistoryRepository extends JpaRepository<ServiceRequestHistoryJpa, UUID> {

    List<ServiceRequestHistoryJpa> findByRequestIdOrderByChangedAtDesc(UUID requestId);
}
