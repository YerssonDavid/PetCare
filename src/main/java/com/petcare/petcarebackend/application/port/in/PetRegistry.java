package com.petcare.petcarebackend.application.port.in;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.ResponseHttp;

public interface PetRegistry {
    ResponseHttp registerPet(RegisterPetRequest request);
}
