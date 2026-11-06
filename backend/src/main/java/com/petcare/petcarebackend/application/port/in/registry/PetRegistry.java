package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

import java.util.UUID;

public interface PetRegistry {
    RegisterResponse registerPet(UUID ownerId, RegisterPetRequest request);
}
