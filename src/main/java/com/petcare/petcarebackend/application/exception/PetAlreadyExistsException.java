package com.petcare.petcarebackend.application.exception;

public class PetAlreadyExistsException extends RuntimeException {
    public PetAlreadyExistsException(String name) {
        super("El propietario ya tiene una mascota llamada '" + name + "'");
    }
}
