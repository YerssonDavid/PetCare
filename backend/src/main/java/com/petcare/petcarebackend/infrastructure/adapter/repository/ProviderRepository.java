package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProviderRepository extends JpaRepository<ProviderJpa, UUID> {
    boolean existsByNameCommercialIgnoreCase(String nameCommercial);
    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProviderJpa p where p.id = :id")
    Optional<ProviderJpa> findByIdForUpdate(@Param("id") UUID id);
    Optional<ProviderJpa> findByEmail(String email);
}
