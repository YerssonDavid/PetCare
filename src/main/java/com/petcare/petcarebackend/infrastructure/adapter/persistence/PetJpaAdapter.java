package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.exception.PetAlreadyExistsException;
import com.petcare.petcarebackend.application.port.out.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.PetRepository;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PetJpaAdapter implements PetRepositoryPort {

    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public void savePet(Pet pet) {

        // Proxy without SELECT: the use case already verified the owner exists
        OwnerJpa owner = ownerRepository.getReferenceById(pet.getOwnerId());

        PetJpa petJpa = new PetJpa(
                pet.getId(),
                owner,
                pet.getName().value(),
                pet.getName().normalized(),
                pet.getTypePet(),
                pet.getAge(),
                pet.getDescription()
        );

        try {
            // Flush now so a concurrent duplicate surfaces here, even inside a wider transaction
            petRepository.saveAndFlush(petJpa);
        } catch (DataIntegrityViolationException ex) {
            if (isOwnerNameViolation(ex)) {
                throw new PetAlreadyExistsException(pet.getName().value());
            }
            throw ex;
        }
    }

    @Override
    public boolean existsByOwnerIdAndNormalizedName(UUID ownerId, String normalizedName) {
        return petRepository.existsByOwnerIdAndNameNormalized(ownerId, normalizedName);
    }

    private boolean isOwnerNameViolation(DataIntegrityViolationException ex) {
        return ex.getCause() instanceof ConstraintViolationException cve
                && PetJpa.UK_OWNER_NAME.equalsIgnoreCase(cve.getConstraintName());
    }
}
