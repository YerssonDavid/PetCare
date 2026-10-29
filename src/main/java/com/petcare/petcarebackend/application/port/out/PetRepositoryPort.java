package com.petcare.petcarebackend.application.port.out;

import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.domain.entity.Pet;

import java.util.UUID;

public interface PetRepositoryPort {
    ResponseHttp savePet(Pet pet);
    boolean existsByOwnerIdAndName(UUID ownerId, String name);
}
