package com.petcare.petcarebackend.application.exception;

public class DuplicateServiceRequestException extends RuntimeException {
    public DuplicateServiceRequestException(String message) {
        super(message);
    }
}
