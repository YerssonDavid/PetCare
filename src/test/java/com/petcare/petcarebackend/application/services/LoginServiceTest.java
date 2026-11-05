package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.exception.AccountNotFoundException;
import com.petcare.petcarebackend.application.exception.InvalidCredentialsException;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.login.JwtPort;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.application.port.out.login.RefreshTokenRepositoryPort;
import com.petcare.petcarebackend.domain.entity.RefreshToken;
import com.petcare.petcarebackend.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String EMAIL = "ana@mail.com";
    private static final String PASSWORD = "secret123";
    private static final String ACCOUNT_HASH = "account-hash";
    private static final String ACCESS_TOKEN = "signed-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String REFRESH_HASH = "refresh-hash";
    private static final Instant REFRESH_EXPIRY = Instant.parse("2026-11-11T13:00:00Z");
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Mock
    private LoginUsersPort loginUsersPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtPort jwtPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    private LoginService service;

    @BeforeEach
    void setUp() {
        service = new LoginService(loginUsersPort, passwordEncoder, new AuthTokenIssuer(jwtPort, refreshTokenRepository));
    }

    private LoginRequest request() {
        return new LoginRequest(EMAIL, PASSWORD);
    }

    private void accountExists(Role role, boolean active) {
        when(loginUsersPort.findByEmail(EMAIL))
                .thenReturn(Optional.of(new LoginAccount(ACCOUNT_ID, ACCOUNT_HASH, role, active)));
    }

    private void tokensAreIssuedFor(Role role) {
        when(jwtPort.generateRefreshToken(ACCOUNT_ID))
                .thenReturn(new IssuedRefreshToken(REFRESH_TOKEN, REFRESH_HASH, REFRESH_EXPIRY));
        when(jwtPort.generateAccessToken(new ClaimsUser(ACCOUNT_ID, role))).thenReturn(ACCESS_TOKEN);
    }

    private void verifyNoTokenIssued() {
        verifyNoInteractions(jwtPort);
        verify(refreshTokenRepository, never()).saveRefreshToken(any());
    }

    @Test
    void returnsBothTokensOfAnOwnerWithValidCredentials() {
        accountExists(Role.USER, true);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(true);
        tokensAreIssuedFor(Role.USER);

        LoginResponse response = service.loginUsers(request());

        assertThat(response.token()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void takesTheRoleFromTheAccountFound() {
        accountExists(Role.PROVIDER, true);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(true);
        tokensAreIssuedFor(Role.PROVIDER);

        assertThat(service.loginUsers(request()).token()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    void storesOnlyTheHashOfTheRefreshToken() {
        accountExists(Role.USER, true);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(true);
        tokensAreIssuedFor(Role.USER);

        service.loginUsers(request());

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveRefreshToken(saved.capture());
        assertThat(saved.getValue().getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getValue().getTokenHash()).isEqualTo(REFRESH_HASH);
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(REFRESH_EXPIRY);
        assertThat(saved.getValue().isRevoked()).isFalse();
    }

    @Test
    void searchesTheAccountWithTheNormalizedEmail() {
        accountExists(Role.USER, true);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(true);
        tokensAreIssuedFor(Role.USER);

        service.loginUsers(new LoginRequest("  ANA@Mail.com ", PASSWORD));

        verify(loginUsersPort).findByEmail(EMAIL);
    }

    @Test
    void rejectsAnEmailWithNoAccountWithoutCheckingThePassword() {
        when(loginUsersPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loginUsers(request()))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("No existe una cuenta con ese correo");
        verifyNoInteractions(passwordEncoder);
        verifyNoTokenIssued();
    }

    @Test
    void rejectsAWrongPassword() {
        accountExists(Role.USER, true);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.loginUsers(request()))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Correo o contraseña incorrectos");
        verifyNoTokenIssued();
    }

    @Test
    void rejectsAnInactiveAccountEvenWithTheRightPassword() {
        accountExists(Role.USER, false);
        when(passwordEncoder.matches(PASSWORD, ACCOUNT_HASH)).thenReturn(true);

        assertThatThrownBy(() -> service.loginUsers(request()))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoTokenIssued();
    }

    @Test
    void rejectsAMalformedEmailWithoutSearchingTheAccount() {
        assertThatThrownBy(() -> service.loginUsers(new LoginRequest("a@b", PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El formato del correo no es válido");
        verifyNoInteractions(loginUsersPort);
    }
}
