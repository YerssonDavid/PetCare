package com.petcare.petcarebackend.domain.valueobject;

import java.util.regex.Pattern;

public record Phone(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\+?[0-9]{7,15}$");

    public Phone {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone is required");
        }
        value = value.replaceAll("[\\s()-]", "");
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Phone must contain 7 to 15 digits, optionally prefixed with +");
        }
    }
}
