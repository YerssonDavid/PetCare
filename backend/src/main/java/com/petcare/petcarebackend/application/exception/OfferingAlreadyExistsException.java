package com.petcare.petcarebackend.application.exception;

public class OfferingAlreadyExistsException extends RuntimeException {
    public OfferingAlreadyExistsException(String name) {
        super("The provider already has a service or product named '" + name + "'");
    }
}
