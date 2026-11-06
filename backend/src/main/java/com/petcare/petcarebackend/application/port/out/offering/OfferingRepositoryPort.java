package com.petcare.petcarebackend.application.port.out.offering;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferingRepositoryPort {
    void saveOffering(Offering offering);
    boolean existsByProviderIdAndNormalizedName(UUID providerId, String normalizedName);
    Optional<Offering> findById(UUID id);
    List<Offering> findAll();
    List<Offering> findByCategory(OfferingCategory category);
}
