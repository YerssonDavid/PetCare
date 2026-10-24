package com.petcare.petcarebackend.infrastructure.services;

import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderService implements PasswordEncoder {

    // Fully qualified: the port shares the simple name with Spring's PasswordEncoder
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public String encode(String password) {
        return passwordEncoder.encode(password);
    }
}
