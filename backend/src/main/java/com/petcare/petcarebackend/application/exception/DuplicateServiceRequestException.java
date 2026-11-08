package com.petcare.petcarebackend.application.exception;

public class DuplicateServiceRequestException extends RuntimeException {
    public DuplicateServiceRequestException() {
        super("Ya existe una solicitud para esta mascota en esa franja");
    }
}
