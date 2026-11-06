package com.petcare.petcarebackend.application.exception;

public class OwnerAlreadyExistsException extends RuntimeException {
    public OwnerAlreadyExistsException() {
        super("Error en el registro");
    }
}
