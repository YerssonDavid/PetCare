package com.petcare.petcarebackend.domain.entity;

import java.time.Instant;
import java.util.UUID;

// Long-lived credential that buys new access tokens. Only its hash is stored, and each use replaces it
public class RefreshToken {

    private UUID id;
    private UUID accountId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant revokedAt;

    public RefreshToken() {
    }

    public RefreshToken(UUID id, UUID accountId, String tokenHash, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.accountId = accountId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public static RefreshToken issue(UUID accountId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), accountId, tokenHash, expiresAt, null);
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
