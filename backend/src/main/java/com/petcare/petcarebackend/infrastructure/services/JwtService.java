package com.petcare.petcarebackend.infrastructure.services;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.application.port.out.login.JwtPort;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// The only class that creates and checks tokens: the signing key and the rules live here, and both
// the security filter (through decode) and the use cases (through JwtPort) go through the same checks
@Component
public class JwtService implements JwtPort, JwtDecoder {

    public static final String ROLE_CLAIM = "role";

    private static final String TYPE_CLAIM = "type";
    private static final String ISSUER = "petcare-backend";
    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    private final JwtEncoder encoder;
    private final Map<TokenType, JwtDecoder> decoders;
    private final Clock clock;

    public JwtService(@Value("${JWT_SECRET}") String secret, Clock clock) {
        SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        this.encoder = NimbusJwtEncoder.withSecretKey(key).build();
        this.clock = clock;
        this.decoders = Map.of(
                TokenType.ACCESS, decoder(key, TokenType.ACCESS),
                TokenType.REFRESH, decoder(key, TokenType.REFRESH)
        );
    }

    @Override
    public String generateAccessToken(ClaimsUser claims) {
        Instant now = clock.instant();
        return sign(commonClaims(claims.id(), TokenType.ACCESS, now, now.plus(ACCESS_TOKEN_DURATION))
                .claim(ROLE_CLAIM, claims.role().name())
                .build());
    }

    // No role inside: the refresh reads it from the account, so a changed role reaches the next access token
    @Override
    public IssuedRefreshToken generateRefreshToken(UUID accountId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(REFRESH_TOKEN_DURATION);
        String token = sign(commonClaims(accountId, TokenType.REFRESH, now, expiresAt).build());
        return new IssuedRefreshToken(token, hash(token), expiresAt);
    }

    // Checks the signature and its algorithm, the expiration and the claims; throws JwtException if any fails
    public Jwt validateToken(String token, TokenType type) {
        return decoders.get(type).decode(token);
    }

    @Override
    public boolean isTokenValid(String token, TokenType type) {
        try {
            validateToken(token, type);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Entry point of the security filter: only an access token opens a protected route
    @Override
    public Jwt decode(String token) {
        return validateToken(token, TokenType.ACCESS);
    }

    // SHA-256 instead of BCrypt: the token cannot be guessed, so there is nothing to brute force,
    // and the same input must give the same hash so the stored token can be looked up
    @Override
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private JwtClaimsSet.Builder commonClaims(UUID accountId, TokenType type, Instant issuedAt, Instant expiresAt) {
        return JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString()) // Two tokens of one account in the same second must still differ
                .issuer(ISSUER)
                .subject(accountId.toString())
                .claim(TYPE_CLAIM, type.name())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt);
    }

    private String sign(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private JwtDecoder decoder(SecretKey key, TokenType type) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(rulesFor(type));
        return decoder;
    }

    // The decoder verifies the signature first, so these rules only ever run on tokens signed with our key
    private OAuth2TokenValidator<Jwt> rulesFor(TokenType type) {
        // No tolerance: the same application issues and validates, so there is no clock difference to absorb
        JwtTimestampValidator expiration = new JwtTimestampValidator(Duration.ZERO);
        expiration.setClock(clock);
        expiration.setAllowEmptyExpiryClaim(false);

        List<OAuth2TokenValidator<Jwt>> rules = new ArrayList<>(List.of(
                expiration,
                new JwtIssuerValidator(ISSUER),
                new JwtClaimValidator<Object>(JwtClaimNames.SUB, JwtService::isUuid),
                new JwtClaimValidator<Object>(JwtClaimNames.JTI, Objects::nonNull),
                new JwtClaimValidator<Instant>(JwtClaimNames.IAT, this::isNotInTheFuture),
                new JwtClaimValidator<Object>(TYPE_CLAIM, type.name()::equals)
        ));
        if (type == TokenType.ACCESS) {
            rules.add(new JwtClaimValidator<Object>(ROLE_CLAIM, JwtService::isRole));
        }
        return new DelegatingOAuth2TokenValidator<>(rules);
    }

    private boolean isNotInTheFuture(Instant issuedAt) {
        return issuedAt != null && !issuedAt.isAfter(clock.instant());
    }

    private static boolean isUuid(Object value) {
        try {
            return value instanceof String text && UUID.fromString(text).toString().equals(text);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isRole(Object value) {
        return Arrays.stream(Role.values()).anyMatch(role -> role.name().equals(value));
    }
}
