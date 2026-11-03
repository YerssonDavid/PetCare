package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.exception.OfferingAlreadyExistsException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OfferingRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import com.petcare.petcarebackend.infrastructure.mapper.OfferingMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OfferingJpaAdapter implements OfferingRepositoryPort {

    private final OfferingRepository offeringRepository;
    private final ProviderRepository providerRepository;

    @Override
    public void saveOffering(Offering offering) {
        // Proxy without SELECT: the use case already verified the provider exists
        ProviderJpa provider = providerRepository.getReferenceById(offering.getProviderId());

        try {
            // Flush now so a concurrent duplicate surfaces here, even inside a wider transaction
            offeringRepository.saveAndFlush(OfferingMapper.toJpa(offering, provider));
        } catch (DataIntegrityViolationException ex) {
            if (isProviderNameViolation(ex)) {
                throw new OfferingAlreadyExistsException(offering.getName().value());
            }
            throw ex;
        }
    }

    @Override
    public boolean existsByProviderIdAndNormalizedName(UUID providerId, String normalizedName) {
        return offeringRepository.existsByProviderIdAndNameNormalized(providerId, normalizedName);
    }

    @Override
    public Optional<Offering> findById(UUID id) {
        return offeringRepository.findById(id).map(OfferingMapper::toDomain);
    }

    @Override
    public List<Offering> findAll() {
        return offeringRepository.findAll().stream()
                .map(OfferingMapper::toDomain)
                .toList();
    }

    @Override
    public List<Offering> findByCategory(OfferingCategory category) {
        return offeringRepository.findByCategory(category).stream()
                .map(OfferingMapper::toDomain)
                .toList();
    }

    private boolean isProviderNameViolation(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException cve
                && OfferingJpa.UK_PROVIDER_NAME.equalsIgnoreCase(cve.getConstraintName());
    }
}
