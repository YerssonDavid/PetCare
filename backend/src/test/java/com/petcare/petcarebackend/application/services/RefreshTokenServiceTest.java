package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.dto.RefreshTokenRequest;
import com.petcare.petcarebackend.application.exception.InvalidRefreshTokenException;
import com.petcare.petcarebackend.application.port.out.login.JwtPort;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.application.port.out.login.RefreshTokenRepositoryPort;
import com.petcare.petcarebackend.domain.entity.RefreshToken;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
class RefreshTokenServiceTest {

    private static final String PRESENTED = "presented-token";
    private static final String PRESENTED_HASH = "presented-hash";
    private static final String NEW_REFRESH = "new-refresh";
    private static final String NEW_HASH = "new-hash";
    private static final String NEW_ACCESS = "new-access";
    private static final UUID TOKEN_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-04T13:00:00Z");
    private static final Instant NEW_EXPIRY = NOW.plus(Duration.ofDays(7));

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private JwtPort jwtPort;

    @Mock
    private LoginUsersPort loginUsersPort;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneId.of("America/Bogota"));
        AuthTokenIssuer issuer = new AuthTokenIssuer(jwtPort, refreshTokenRepository);
        service = new RefreshTokenService(refreshTokenRepository, jwtPort, loginUsersPort, issuer, clock);
    }

    private RefreshTokenRequest request() {
        return new RefreshTokenRequest(PRESENTED);
    }

    private void tokenPassesTheJwtChecks() {
        when(jwtPort.isTokenValid(PRESENTED, TokenType.REFRESH)).thenReturn(true);
        when(jwtPort.hash(PRESENTED)).thenReturn(PRESENTED_HASH);
    }

    private void storedToken(Instant expiresAt, Instant revokedAt) {
        tokenPassesTheJwtChecks();
        when(refreshTokenRepository.findByTokenHash(PRESENTED_HASH))
                .thenReturn(Optional.of(new RefreshToken(TOKEN_ID, ACCOUNT_ID, PRESENTED_HASH, expiresAt, revokedAt)));
    }

    private void storedActiveToken() {
        storedToken(NOW.plus(Duration.ofDays(1)), null);
    }

    private void accountIs(Role role, boolean active) {
        when(loginUsersPort.findById(ACCOUNT_ID))
                .thenReturn(Optional.of(new LoginAccount(ACCOUNT_ID, "password-hash", role, active)));
    }

    private void newTokensAreIssuedFor(Role role) {
        when(jwtPort.generateRefreshToken(ACCOUNT_ID)).thenReturn(new IssuedRefreshToken(NEW_REFRESH, NEW_HASH, NEW_EXPIRY));
        when(jwtPort.generateAccessToken(new ClaimsUser(ACCOUNT_ID, role))).thenReturn(NEW_ACCESS);
    }

    private void assertRejected() {
        assertThatThrownBy(() -> service.refreshToken(request()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("La sesión no es válida o ya expiró; inicia sesión de nuevo");
        verify(jwtPort, never()).generateAccessToken(any());
        verify(jwtPort, never()).generateRefreshToken(any());
        verify(refreshTokenRepository, never()).saveRefreshToken(any());
    }

    @Test
    void replacesTheTokenWithANewPair() {
        storedActiveToken();
        when(refreshTokenRepository.revokeIfActive(TOKEN_ID, NOW)).thenReturn(true);
        accountIs(Role.USER, true);
        newTokensAreIssuedFor(Role.USER);

        LoginResponse response = service.refreshToken(request());

        assertThat(response.token()).isEqualTo(NEW_ACCESS);
        assertThat(response.refreshToken()).isEqualTo(NEW_REFRESH);
        verify(refreshTokenRepository).revokeIfActive(TOKEN_ID, NOW);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).saveRefreshToken(saved.capture());
        assertThat(saved.getValue().getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(saved.getValue().getTokenHash()).isEqualTo(NEW_HASH);
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NEW_EXPIRY);
    }

    @Test
    void takesTheRoleFromTheCurrentAccountAndNotFromTheOldToken() {
        storedActiveToken();
        when(refreshTokenRepository.revokeIfActive(TOKEN_ID, NOW)).thenReturn(true);
        accountIs(Role.PROVIDER, true);
        newTokensAreIssuedFor(Role.PROVIDER);

        assertThat(service.refreshToken(request()).token()).isEqualTo(NEW_ACCESS);
    }

    // Forged, expired or of the wrong type: JwtPort says no and nothing else happens
    @Test
    void rejectsATokenThatFailsTheJwtChecksWithoutTouchingTheDatabase() {
        when(jwtPort.isTokenValid(PRESENTED, TokenType.REFRESH)).thenReturn(false);

        assertRejected();
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void rejectsAValidJwtThatWasNeverStored() {
        tokenPassesTheJwtChecks();
        when(refreshTokenRepository.findByTokenHash(PRESENTED_HASH)).thenReturn(Optional.empty());

        assertRejected();
        verify(refreshTokenRepository, never()).revokeAllActiveByAccount(any(), any());
    }

    @Test
    void rejectsAnExpiredTokenWithoutEndingOtherSessions() {
        storedToken(NOW, null);

        assertRejected();
        verify(refreshTokenRepository, never()).revokeIfActive(any(), any());
        verify(refreshTokenRepository, never()).revokeAllActiveByAccount(any(), any());
    }

    @Test
    void endsEverySessionOfTheAccountWhenAUsedTokenAppearsAgain() {
        storedToken(NOW.plus(Duration.ofDays(1)), NOW.minusSeconds(60));

        assertRejected();
        verify(refreshTokenRepository).revokeAllActiveByAccount(ACCOUNT_ID, NOW);
    }

    // The rejection must not undo that revocation
    @Test
    void doesNotRollBackWhenRejectingAToken() throws NoSuchMethodException {
        Transactional transactional = RefreshTokenService.class
                .getMethod("refreshToken", RefreshTokenRequest.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional.noRollbackFor()).contains(InvalidRefreshTokenException.class);
    }

    // Two simultaneous uses: the loser is rejected, but the winner's new session must survive
    @Test
    void rejectsTheLoserOfASimultaneousUseWithoutEndingTheSessions() {
        storedActiveToken();
        when(refreshTokenRepository.revokeIfActive(TOKEN_ID, NOW)).thenReturn(false);

        assertRejected();
        verify(refreshTokenRepository, never()).revokeAllActiveByAccount(any(), any());
    }

    @Test
    void rejectsATokenOfADeactivatedAccount() {
        storedActiveToken();
        when(refreshTokenRepository.revokeIfActive(TOKEN_ID, NOW)).thenReturn(true);
        accountIs(Role.USER, false);

        assertRejected();
    }

    @Test
    void rejectsATokenOfADeletedAccount() {
        storedActiveToken();
        when(refreshTokenRepository.revokeIfActive(TOKEN_ID, NOW)).thenReturn(true);
        when(loginUsersPort.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertRejected();
    }
}
