package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.RefreshToken;
import com.petcare.petcarebackend.infrastructure.entity.RefreshTokenJpa;

public final class RefreshTokenMapper {

    private RefreshTokenMapper() {
    }

    public static RefreshTokenJpa toJpa(RefreshToken token) {
        return new RefreshTokenJpa(
                token.getId(),
                token.getAccountId(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getRevokedAt()
        );
    }

    public static RefreshToken toDomain(RefreshTokenJpa entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getAccountId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }
}
