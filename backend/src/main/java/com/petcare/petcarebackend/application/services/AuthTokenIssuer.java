package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.port.out.login.JwtPort;
import com.petcare.petcarebackend.application.port.out.login.RefreshTokenRepositoryPort;
import com.petcare.petcarebackend.domain.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Shared by login and refresh: both end by giving the account a new pair of tokens
@Service
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtPort jwtPort;
    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public LoginResponse issue(LoginAccount account) {
        IssuedRefreshToken refreshToken = jwtPort.generateRefreshToken(account.id());
        refreshTokenRepository.saveRefreshToken(
                RefreshToken.issue(account.id(), refreshToken.tokenHash(), refreshToken.expiresAt()));

        String accessToken = jwtPort.generateAccessToken(new ClaimsUser(account.id(), account.role()));
        return new LoginResponse(accessToken, refreshToken.token());
    }
}
