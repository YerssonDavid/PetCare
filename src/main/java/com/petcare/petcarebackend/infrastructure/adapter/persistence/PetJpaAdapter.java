package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.application.port.out.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.PetRepository;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PetJpaAdapter implements PetRepositoryPort {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public ResponseHttp savePet(Pet pet) {

        // Proxy without SELECT: the use case already verified the owner exists
        OwnerJpa owner = ownerRepository.getReferenceById(pet.getOwnerId());

        PetJpa petJpa = new PetJpa(
                pet.getId(),
                owner,
                pet.getName(),
                pet.getTypePet(),
                pet.getAge(),
                pet.getDescription()
        );

        petRepository.save(petJpa);
        return new ResponseHttp("Pet registered successfully", true, LocalDateTime.now());
    }

    @Override
    public boolean existsByOwnerIdAndName(UUID ownerId, String name) {
        return petRepository.existsByOwnerIdAndName(ownerId, name);
    }

}
