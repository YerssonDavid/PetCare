package com.petcare.petcarebackend.domain.valueobject;

import java.util.Locale;

public record PetName(String value) {

    private static final int MAX_LENGTH = 100;

    public PetName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pet name is required");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Pet name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    // Comparison key for "one name per owner": "Firulais" and "FIRULAIS" collide
    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }
}
