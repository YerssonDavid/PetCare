package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

public interface PetRegistry {
    RegisterResponse registerPet(RegisterPetRequest request);
}
