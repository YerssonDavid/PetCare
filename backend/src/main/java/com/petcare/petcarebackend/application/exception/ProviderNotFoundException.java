package com.petcare.petcarebackend.application.exception;

import java.util.UUID;

public class ProviderNotFoundException extends RuntimeException {
    public ProviderNotFoundException(UUID providerId) {
        super("Proveedor no encontrado: " + providerId);
    }
}
