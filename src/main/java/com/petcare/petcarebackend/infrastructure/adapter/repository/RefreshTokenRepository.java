package com.petcare.petcarebackend.infrastructure.adapter.repository;

import com.petcare.petcarebackend.infrastructure.entity.RefreshTokenJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenJpa, UUID> {

    Optional<RefreshTokenJpa> findByTokenHash(String tokenHash);

    // Conditional update: the database decides the winner when one token is used twice at the same time
    @Modifying(clearAutomatically = true)
    @Query("update RefreshTokenJpa t set t.revokedAt = :now where t.id = :id and t.revokedAt is null")
    int revokeIfActive(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("update RefreshTokenJpa t set t.revokedAt = :now where t.accountId = :accountId and t.revokedAt is null")
    int revokeAllActiveByAccount(@Param("accountId") UUID accountId, @Param("now") Instant now);
}
