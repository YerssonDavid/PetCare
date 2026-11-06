package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferingRepository extends JpaRepository<OfferingJpa, UUID> {
    boolean existsByProviderIdAndNameNormalized(UUID providerId, String nameNormalized);
    List<OfferingJpa> findByCategory(OfferingCategory category);
}
