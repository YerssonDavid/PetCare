package com.petcare.petcarebackend.infrastructure.services;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordEncoderServiceTest {

    private final PasswordEncoderService service = new PasswordEncoderService(new BCryptPasswordEncoder());

    @Test
    void encodesWithBCryptWithoutKeepingThePassword() {
        String encoded = service.encode("password123");

        assertThat(encoded).startsWith("$2").doesNotContain("password123");
    }

    @Test
    void matchesOnlyTheOriginalPassword() {
        String encoded = service.encode("password123");

        assertThat(service.matches("password123", encoded)).isTrue();
        assertThat(service.matches("password124", encoded)).isFalse();
    }

    @Test
    void acceptsAPasswordOfExactlyTheBcryptLimit() {
        String encoded = service.encode("a".repeat(72));

        assertThat(service.matches("a".repeat(72), encoded)).isTrue();
    }

    // 72 characters pass the size validation of the request, but "ñ" takes two bytes
    @Test
    void rejectsAPasswordOverTheBcryptLimitInBytesWithAMessageForTheClient() {
        assertThatThrownBy(() -> service.encode("ñ".repeat(72)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La contraseña es demasiado larga");
    }
}
