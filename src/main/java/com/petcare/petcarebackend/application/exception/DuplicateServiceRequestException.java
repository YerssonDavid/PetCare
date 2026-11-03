package com.petcare.petcarebackend.application.exception;

public class DuplicateServiceRequestException extends RuntimeException {
    public DuplicateServiceRequestException() {
        super("The pet already has an active service request at that time");
    }
}
