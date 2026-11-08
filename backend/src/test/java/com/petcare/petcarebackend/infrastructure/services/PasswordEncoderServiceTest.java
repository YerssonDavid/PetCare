package com.petcare.petcarebackend.infrastructure.services;

import org.junit.jupiter.api.Test;
import com.petcare.petcarebackend.infrastructure.configuration.SecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderServiceTest {

    private final PasswordEncoderService service =
            new PasswordEncoderService(new SecurityConfig().passwordEncoder());

    @Test
    void encodesWithoutKeepingThePassword() {
        String encoded = service.encode("password123");

        assertThat(encoded).doesNotContain("password123");
    }

    @Test
    void matchesOnlyTheOriginalPassword() {
        String encoded = service.encode("password123");

        assertThat(service.matches("password123", encoded)).isTrue();
        assertThat(service.matches("password124", encoded)).isFalse();
    }

}
