package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetAlreadyExistsException;
import com.petcare.petcarebackend.application.port.in.registry.PetRegistry;
import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class RegisterPetService implements PetRegistry {

    private final PetRepositoryPort petRepository;
    private final OwnerRepositoryPort ownerRepository;

    @Override
    public RegisterResponse registerPet(RegisterPetRequest request) {
        if(!ownerRepository.existsById(request.ownerId())){
            throw new OwnerNotFoundException(request.ownerId());
        }

        PetName name = new PetName(request.name());

        if(petRepository.existsByOwnerIdAndNormalizedName(request.ownerId(), name.normalized())){
            throw new PetAlreadyExistsException(name.value());
        }

        Pet pet = Pet.register(
                request.ownerId(),
                name,
                request.typePet(),
                request.age(),
                request.description()
        );

        petRepository.savePet(pet);
        return new RegisterResponse(pet.getId(), "Pet registered successfully", LocalDateTime.now());
    }
}
