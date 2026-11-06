package com.petcare.petcarebackend.domain.valueobject;

import java.util.Locale;

public record CommercialName(String value) {

    private static final int MAX_LENGTH = 100;

    public CommercialName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El nombre comercial es obligatorio");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El nombre comercial no puede superar " + MAX_LENGTH + " caracteres");
        }
    }

    // Comparison key for "one provider per name": "Vet Sol" and " VET  sol " collide
    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }
}
