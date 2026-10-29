package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.application.port.in.PetRegistry;
import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class RegisterPetService implements PetRegistry {

    private final PetRepositoryPort petRepository;
    private final OwnerRepositoryPort ownerRepository;

    @Override
    public ResponseHttp registerPet(RegisterPetRequest request) {
        if(!ownerRepository.existsById(request.ownerId())){
            return new ResponseHttp("Owner not found", false, LocalDateTime.now());
        }

        if(petRepository.existsByOwnerIdAndName(request.ownerId(), request.name())){
            return new ResponseHttp("Pet already exists", false, LocalDateTime.now());
        }

        // Create pet
        Pet pet = Pet.register(
                request.ownerId(),
                request.name(),
                request.typePet(),
                request.age(),
                request.description()
        );

        return petRepository.savePet(pet);
    }
}
