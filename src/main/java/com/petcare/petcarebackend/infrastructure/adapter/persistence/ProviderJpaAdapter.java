package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.ProviderRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import com.petcare.petcarebackend.infrastructure.mapper.ProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProviderJpaAdapter implements ProviderRepositoryPort {

    private final ProviderRepository providerRepository;

    @Override
    public void saveProvider(Provider provider) {
        ProviderJpa providerJpa = ProviderMapper.toJpa(provider);
        providerRepository.save(providerJpa);
    }

    @Override
    public boolean existsByName(String name) {
        return providerRepository.existsByNameCommercialIgnoreCase(name);
    }

    @Override
    public boolean existsByEmail(String email) {
        return providerRepository.existsByEmail(email);
    }
}
