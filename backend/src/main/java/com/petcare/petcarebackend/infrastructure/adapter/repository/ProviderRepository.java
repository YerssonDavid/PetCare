package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProviderRepository extends JpaRepository<ProviderJpa, UUID> {
    boolean existsByNameCommercialIgnoreCase(String nameCommercial);
    boolean existsByEmail(String email);
}
