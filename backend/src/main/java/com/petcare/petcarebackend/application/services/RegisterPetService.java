package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetAlreadyExistsException;
import com.petcare.petcarebackend.application.port.in.registry.PetRegistry;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.pet.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RegisterPetService implements PetRegistry {

    private final PetRepositoryPort petRepository;
    private final OwnerRepositoryPort ownerRepository;

    @Override
    public RegisterResponse registerPet(UUID ownerId, RegisterPetRequest request) {
        if(!ownerRepository.existsById(ownerId)){
            throw new OwnerNotFoundException(ownerId);
        }

        PetName name = new PetName(request.name());

        if(petRepository.existsByOwnerIdAndNormalizedName(ownerId, name.normalized())){
            throw new PetAlreadyExistsException(name.value());
        }

        Pet pet = Pet.register(
                ownerId,
                name,
                request.typePet(),
                request.age(),
                request.description()
        );

        petRepository.savePet(pet);
        return new RegisterResponse(pet.getId(), "La mascota se ha registrado correctamente", LocalDateTime.now());
    }
}
