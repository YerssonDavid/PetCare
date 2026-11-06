package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.PetResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.port.in.query.PetQuery;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.pet.PetRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListPetsService implements PetQuery {

    private final PetRepositoryPort petRepository;
    private final OwnerRepositoryPort ownerRepository;

    @Override
    public List<PetResponse> listPetsByOwner(UUID ownerId) {
        if (!ownerRepository.existsById(ownerId)) {
            throw new OwnerNotFoundException(ownerId);
        }

        return petRepository.findPetsByIdOwner(ownerId).stream()
                .map(PetResponse::from)
                .toList();
    }
}
