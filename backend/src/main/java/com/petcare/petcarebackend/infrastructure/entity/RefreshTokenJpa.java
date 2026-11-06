package com.petcare.petcarebackend.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "idx_refresh_tokens_account_id", columnList = "account_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenJpa {

    @Id
    private UUID id;

    // An owner or a provider: accounts live in two tables, so a foreign key cannot cover both
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    // Only the hash is stored, so a leaked table does not expose usable tokens
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
