package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OwnerJpaAdapter implements OwnerRepositoryPort {

    private final OwnerRepository ownerRepository;

    @Override
    public void saveOwner(Owner owner) {

        OwnerJpa entity = new OwnerJpa(
                owner.getId(),
                owner.getName(),
                owner.getEmail().value(),
                owner.getPhone().value(),
                owner.getPasswordHash(),
                owner.getRole(),
                owner.getActiveAccount(),
                owner.getCreatedAt(),
                new ArrayList<>()
        );

        ownerRepository.save(entity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return ownerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return ownerRepository.existsById(id);
    }
}
