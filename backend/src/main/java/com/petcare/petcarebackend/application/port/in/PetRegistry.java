package com.petcare.petcarebackend.application.port.in;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterPetResponse;

public interface PetRegistry {
    RegisterPetResponse registerPet(RegisterPetRequest request);
}
