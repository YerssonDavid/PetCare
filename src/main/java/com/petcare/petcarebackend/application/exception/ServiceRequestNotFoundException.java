package com.petcare.petcarebackend.application.exception;

import java.util.UUID;

public class ServiceRequestNotFoundException extends RuntimeException {
    public ServiceRequestNotFoundException(UUID id) {
        super("Service request not found: " + id);
    }
}
