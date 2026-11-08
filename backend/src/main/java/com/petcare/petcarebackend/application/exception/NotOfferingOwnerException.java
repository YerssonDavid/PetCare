package com.petcare.petcarebackend.application.exception;

public class NotOfferingOwnerException extends RuntimeException {
    public NotOfferingOwnerException() {
        super("The service or product belongs to another provider");
    }
}
