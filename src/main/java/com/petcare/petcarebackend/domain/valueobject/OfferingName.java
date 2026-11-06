package com.petcare.petcarebackend.domain.valueobject;

import java.util.Locale;

public record OfferingName(String value) {

    private static final int MAX_LENGTH = 100;

    public OfferingName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio");
        }
        value = value.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El nombre del servicio no puede superar " + MAX_LENGTH + " caracteres");
        }
    }

    // Comparison key for "one name per provider": "Baño Completo" and "baño  completo" collide
    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }
}
