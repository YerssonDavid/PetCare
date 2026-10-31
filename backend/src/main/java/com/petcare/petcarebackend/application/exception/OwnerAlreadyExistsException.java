package com.petcare.petcarebackend.application.exception;

public class OwnerAlreadyExistsException extends RuntimeException {
    // Generic on purpose: the response must not reveal which emails are registered
    public OwnerAlreadyExistsException() {
        super("Error en el registro");
    }
}
