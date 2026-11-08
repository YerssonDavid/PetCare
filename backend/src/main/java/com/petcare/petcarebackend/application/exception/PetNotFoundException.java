package com.petcare.petcarebackend.application.exception;

import java.util.UUID;

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(UUID petId) {
        super("Mascota no encontrada: " + petId);
    }
}
