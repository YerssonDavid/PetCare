package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.dto.RefreshTokenRequest;
import com.petcare.petcarebackend.application.exception.InvalidRefreshTokenException;
import com.petcare.petcarebackend.application.port.in.login.TokenRefresher;
import com.petcare.petcarebackend.application.port.out.login.JwtPort;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.application.port.out.login.RefreshTokenRepositoryPort;
import com.petcare.petcarebackend.domain.entity.RefreshToken;
import com.petcare.petcarebackend.domain.enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements TokenRefresher {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtPort jwtPort;
    private final LoginUsersPort loginUsersPort;
    private final AuthTokenIssuer authTokenIssuer;
    private final Clock clock;

    // No rollback for the rejection: revoking the sessions of a reused token must survive the exception
    @Override
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // Signature, expiration and claims first, so a forged token never reaches the database
        if (!jwtPort.isTokenValid(request.refreshToken(), TokenType.REFRESH)) {
            throw new InvalidRefreshTokenException();
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHash(jwtPort.hash(request.refreshToken()))
                .orElseThrow(InvalidRefreshTokenException::new);
        Instant now = clock.instant();

        // Each token works once, so seeing a used one again means it leaked: end every session of the account
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllActiveByAccount(stored.getAccountId(), now);
            throw new InvalidRefreshTokenException();
        }
        if (stored.isExpired(now) || !refreshTokenRepository.revokeIfActive(stored.getId(), now)) {
            throw new InvalidRefreshTokenException();
        }

        // Read again so a deactivated account stops here and a changed role reaches the new token
        LoginAccount account = loginUsersPort.findById(stored.getAccountId())
                .filter(LoginAccount::active)
                .orElseThrow(InvalidRefreshTokenException::new);

        return authTokenIssuer.issue(account);
    }
}
