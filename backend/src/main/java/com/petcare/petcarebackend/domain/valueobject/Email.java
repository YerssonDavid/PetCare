package com.petcare.petcarebackend.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final int MAX_LENGTH = 150;
    private static final Pattern FORMAT = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("El correo no puede superar " + MAX_LENGTH + " caracteres");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("El formato del correo no es válido");
        }
    }
}
