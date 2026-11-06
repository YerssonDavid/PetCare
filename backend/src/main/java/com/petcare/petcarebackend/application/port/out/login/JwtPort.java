package com.petcare.petcarebackend.application.port.out.login;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.domain.enums.TokenType;

import java.util.UUID;

public interface JwtPort {
    String generateAccessToken(ClaimsUser claims);
    IssuedRefreshToken generateRefreshToken(UUID accountId);
    // Checks the signature, the expiration and the claims, including that the token is of this type
    boolean isTokenValid(String token, TokenType type);
    String hash(String token);
}
