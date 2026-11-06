package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.PetResponse;

import java.util.List;
import java.util.UUID;

public interface PetQuery {
    List<PetResponse> listPetsByOwner(UUID ownerId);
}
