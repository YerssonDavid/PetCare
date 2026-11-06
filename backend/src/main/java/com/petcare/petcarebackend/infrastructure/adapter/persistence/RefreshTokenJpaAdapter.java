package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.login.RefreshTokenRepositoryPort;
import com.petcare.petcarebackend.domain.entity.RefreshToken;
import com.petcare.petcarebackend.infrastructure.adapter.repository.RefreshTokenRepository;
import com.petcare.petcarebackend.infrastructure.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenJpaAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(RefreshTokenMapper.toJpa(refreshToken));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash).map(RefreshTokenMapper::toDomain);
    }

    @Override
    public boolean revokeIfActive(UUID id, Instant now) {
        return refreshTokenRepository.revokeIfActive(id, now) == 1;
    }

    @Override
    public void revokeAllActiveByAccount(UUID accountId, Instant now) {
        refreshTokenRepository.revokeAllActiveByAccount(accountId, now);
    }
}
