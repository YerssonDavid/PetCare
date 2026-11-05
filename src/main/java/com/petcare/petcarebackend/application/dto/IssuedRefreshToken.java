package com.petcare.petcarebackend.application.dto;

import java.time.Instant;

// The token goes to the client; only its hash and expiry are stored
public record IssuedRefreshToken(
        String token,
        String tokenHash,
        Instant expiresAt
) {
}
