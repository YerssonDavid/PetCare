package com.petcare.petcarebackend.infrastructure.services;

import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class PasswordEncoderService implements PasswordEncoder {

    private static final int BCRYPT_MAX_BYTES = 72;

    // Fully qualified: the port shares the simple name with Spring's PasswordEncoder
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public String encode(String password) {
        // BCrypt limits bytes, not characters: 72 accented characters pass the size validation but not this
        if (password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            throw new IllegalArgumentException("La contraseña es demasiado larga");
        }
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean matches(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }
}
