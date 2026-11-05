package com.petcare.petcarebackend.application.exception;

public class ProviderAlreadyExistsException extends RuntimeException {

    public ProviderAlreadyExistsException(String message) {
        super(message);

    }
}
