package com.petcare.petcarebackend.application.exception;

public class NotRequestProviderException extends RuntimeException {
    public NotRequestProviderException() {
        super("The request belongs to another provider");
    }
}
