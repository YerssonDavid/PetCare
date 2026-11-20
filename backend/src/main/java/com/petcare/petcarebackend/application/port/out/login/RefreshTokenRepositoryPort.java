package com.petcare.petcarebackend.application.port.out.login;

import com.petcare.petcarebackend.domain.entity.RefreshToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {
    void saveRefreshToken(RefreshToken refreshToken);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    // Atomic: when one token is used twice at the same time, only one call returns true
    boolean revokeIfActive(UUID id, Instant now);
    void revokeAllActiveByAccount(UUID accountId, Instant now);
}
