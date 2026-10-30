package com.petcare.petcarebackend.application.exception;

public class PetAlreadyExistsException extends RuntimeException {
    public PetAlreadyExistsException(String name) {
        super("The owner already has a pet named '" + name + "'");
    }
}
