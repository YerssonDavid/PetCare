package com.petcare.petcarebackend.domain.valueobject;

import java.util.Locale;

public record CommercialName(String value) {

    private static final int MAX_LENGTH = 100;

    public CommercialName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Commercial name is required");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Commercial name must not exceed " + MAX_LENGTH + " characters");
        }
    }

    // Comparison key for "one provider per name": "Vet Sol" and " VET  sol " collide
    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }
}
