package com.petcare.petcarebackend.application.port.in.login;

import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.dto.RefreshTokenRequest;

public interface TokenRefresher {
    LoginResponse refreshToken(RefreshTokenRequest request);
}
