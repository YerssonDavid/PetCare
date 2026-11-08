package com.petcare.petcarebackend.application.exception;

public class PetNotOwnedException extends RuntimeException {
    public PetNotOwnedException() {
        super("La mascota pertenece a otro propietario");
    }
}
