package com.petcare.petcarebackend.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-11-04T13:00:00Z");
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    private RefreshToken issued() {
        return RefreshToken.issue(ACCOUNT_ID, "hash", NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void startsActiveAndExpiresAfterItsDuration() {
        RefreshToken token = issued();

        assertThat(token.getId()).isNotNull();
        assertThat(token.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(token.getTokenHash()).isEqualTo("hash");
        assertThat(token.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(token.isRevoked()).isFalse();
    }

    @Test
    void isValidUntilJustBeforeItsExpiry() {
        assertThat(issued().isExpired(NOW.plus(Duration.ofDays(7)).minusSeconds(1))).isFalse();
    }

    @Test
    void isExpiredAtTheExactExpiry() {
        assertThat(issued().isExpired(NOW.plus(Duration.ofDays(7)))).isTrue();
    }

    @Test
    void isRevokedOnceItHasARevocationDate() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), ACCOUNT_ID, "hash", NOW.plus(Duration.ofDays(7)), NOW);

        assertThat(token.isRevoked()).isTrue();
    }
}
