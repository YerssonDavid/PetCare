package com.petcare.petcarebackend.application.exception;

import java.util.UUID;

public class OfferingNotFoundException extends RuntimeException {
    public OfferingNotFoundException(UUID offeringId) {
        super("Service or product not found: " + offeringId);
    }
}
