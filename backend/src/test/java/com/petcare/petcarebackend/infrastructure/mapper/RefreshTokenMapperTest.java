package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenMapperTest {

    @Test
    void keepsEveryFieldBothWays() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "hash",
                Instant.parse("2026-11-11T13:00:00Z"), Instant.parse("2026-11-05T13:00:00Z"));

        RefreshToken mapped = RefreshTokenMapper.toDomain(RefreshTokenMapper.toJpa(token));

        assertThat(mapped).usingRecursiveComparison().isEqualTo(token);
    }
}
