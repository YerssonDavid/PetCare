package com.petcare.petcarebackend.infrastructure.services;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.dto.IssuedRefreshToken;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JwtServiceTest {

    private static final String SECRET = secretOf((byte) 1);
    private static final String OTHER_SECRET = secretOf((byte) 2);
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-11-10T13:00:00Z");

    private final JwtService service = at(NOW);

    private static String secretOf(byte value) {
        byte[] key = new byte[32];
        Arrays.fill(key, value);
        return Base64.getEncoder().encodeToString(key);
    }

    private static JwtService at(Instant instant) {
        return new JwtService(SECRET, Clock.fixed(instant, ZoneId.of("America/Bogota")));
    }

    private String accessToken(Role role) {
        return service.generateAccessToken(new ClaimsUser(ACCOUNT_ID, role));
    }

    private String refreshToken() {
        return service.generateRefreshToken(ACCOUNT_ID).token();
    }

    // ------------------------------------------------------------ generation

    @Test
    void anAccessTokenCarriesTheAccountRoleAndOneHourOfLife() {
        Jwt jwt = service.validateToken(accessToken(Role.PROVIDER), TokenType.ACCESS);

        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(jwt.getSubject()).isEqualTo(ACCOUNT_ID.toString());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("PROVIDER");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("ACCESS");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("petcare-backend");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
    }

    // Anyone holding a token can read its payload, so nothing else may travel in it
    @Test
    void anAccessTokenCarriesOnlyTheExpectedClaims() {
        Jwt jwt = service.validateToken(accessToken(Role.USER), TokenType.ACCESS);

        assertThat(jwt.getClaims()).containsOnlyKeys("jti", "iss", "sub", "type", "role", "iat", "exp");
    }

    @Test
    void aRefreshTokenHasSevenDaysOfLifeAndNoRole() {
        IssuedRefreshToken issued = service.generateRefreshToken(ACCOUNT_ID);

        Jwt jwt = service.validateToken(issued.token(), TokenType.REFRESH);

        assertThat(jwt.getSubject()).isEqualTo(ACCOUNT_ID.toString());
        assertThat(jwt.getClaimAsString("type")).isEqualTo("REFRESH");
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
        assertThat(jwt.getClaims()).containsOnlyKeys("jti", "iss", "sub", "type", "iat", "exp");
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(7)));
    }

    @Test
    void aRefreshTokenComesWithTheHashToStore() {
        IssuedRefreshToken issued = service.generateRefreshToken(ACCOUNT_ID);

        assertThat(issued.tokenHash()).isEqualTo(service.hash(issued.token())).isNotEqualTo(issued.token());
    }

    // The stored hash is unique, so two logins of one account in the same second must not collide
    @Test
    void twoTokensOfOneAccountAtTheSameInstantDiffer() {
        assertThat(refreshToken()).isNotEqualTo(refreshToken());
        assertThat(accessToken(Role.USER)).isNotEqualTo(accessToken(Role.USER));
    }

    @Test
    void hashesWithSha256() {
        assertThat(service.hash("abc")).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    // ------------------------------------------------------------ validation: type

    @Test
    void acceptsEachTokenAsItsOwnType() {
        assertThat(service.isTokenValid(accessToken(Role.USER), TokenType.ACCESS)).isTrue();
        assertThat(service.isTokenValid(refreshToken(), TokenType.REFRESH)).isTrue();
    }

    @Test
    void aRefreshTokenIsNotAnAccessToken() {
        assertThat(service.isTokenValid(refreshToken(), TokenType.ACCESS)).isFalse();
    }

    @Test
    void anAccessTokenIsNotARefreshToken() {
        assertThat(service.isTokenValid(accessToken(Role.USER), TokenType.REFRESH)).isFalse();
    }

    @Test
    void theSecurityFilterEntryPointOnlyAcceptsAccessTokens() {
        assertThat(service.decode(accessToken(Role.USER)).getSubject()).isEqualTo(ACCOUNT_ID.toString());
        // The filter answers 401 only for this exception; any other JwtException would become a server error
        assertThatThrownBy(() -> service.decode(refreshToken())).isInstanceOf(BadJwtException.class);
    }

    // ------------------------------------------------------------ validation: expiration

    @Test
    void anAccessTokenExpiresAfterOneHour() {
        String token = accessToken(Role.USER);

        assertThat(at(NOW.plus(Duration.ofHours(1)).minusSeconds(1)).isTokenValid(token, TokenType.ACCESS)).isTrue();
        assertThat(at(NOW.plus(Duration.ofHours(1)).plusSeconds(1)).isTokenValid(token, TokenType.ACCESS)).isFalse();
    }

    @Test
    void aRefreshTokenExpiresAfterSevenDays() {
        String token = refreshToken();

        assertThat(at(NOW.plus(Duration.ofDays(7)).minusSeconds(1)).isTokenValid(token, TokenType.REFRESH)).isTrue();
        assertThat(at(NOW.plus(Duration.ofDays(7)).plusSeconds(1)).isTokenValid(token, TokenType.REFRESH)).isFalse();
    }

    @Test
    void validateTokenExplainsThatTheTokenIsInvalidByThrowing() {
        String token = accessToken(Role.USER);
        JwtService later = at(NOW.plus(Duration.ofHours(2)));

        assertThatThrownBy(() -> later.validateToken(token, TokenType.ACCESS)).isInstanceOf(JwtException.class);
    }

    // ------------------------------------------------------------ validation: signature

    @ParameterizedTest
    @EnumSource(TokenType.class)
    void rejectsATokenSignedWithAnotherKey(TokenType type) {
        JwtService other = new JwtService(OTHER_SECRET, Clock.fixed(NOW, ZoneId.of("America/Bogota")));
        String token = type == TokenType.ACCESS
                ? other.generateAccessToken(new ClaimsUser(ACCOUNT_ID, Role.USER))
                : other.generateRefreshToken(ACCOUNT_ID).token();

        assertThat(service.isTokenValid(token, type)).isFalse();
    }

    @Test
    void rejectsATokenWhosePayloadWasChanged() {
        String[] parts = accessToken(Role.USER).split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String forged = base64Url(payload.replace("\"USER\"", "\"PROVIDER\""));

        assertThat(forged).isNotEqualTo(parts[1]);
        assertThat(service.isTokenValid(parts[0] + "." + forged + "." + parts[2], TokenType.ACCESS)).isFalse();
    }

    @Test
    void rejectsATokenWithoutSignature() {
        String[] parts = accessToken(Role.USER).split("\\.");
        String unsigned = base64Url("{\"alg\":\"none\"}") + "." + parts[1] + ".";

        assertThat(service.isTokenValid(unsigned, TokenType.ACCESS)).isFalse();
    }

    private static String base64Url(String text) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------ validation: claims

    private static Consumer<Map<String, Object>> without(String claim) {
        return claims -> claims.remove(claim);
    }

    private static Consumer<Map<String, Object>> with(String claim, Object value) {
        return claims -> claims.put(claim, value);
    }

    // Signed with the right key and not expired: only the claims can give them away
    private static Stream<Arguments> tokensWithBadClaims() {
        return Stream.of(
                arguments("no expiration", without("exp")),
                arguments("no issue date", without("iat")),
                arguments("issued in the future", with("iat", NOW.plusSeconds(60))),
                arguments("no issuer", without("iss")),
                arguments("another issuer", with("iss", "someone-else")),
                arguments("no subject", without("sub")),
                arguments("subject that is not an account ID", with("sub", "ana@mail.com")),
                arguments("no token ID", without("jti")),
                arguments("no type", without("type")),
                arguments("unknown type", with("type", "ADMIN")),
                arguments("no role", without("role")),
                arguments("unknown role", with("role", "SUPERUSER")),
                arguments("role in the wrong format", with("role", 7))
        );
    }

    private String signedAccessToken(Consumer<Map<String, Object>> change) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer("petcare-backend")
                .subject(ACCOUNT_ID.toString())
                .claim("type", "ACCESS")
                .claim("role", "USER")
                .issuedAt(NOW)
                .expiresAt(NOW.plus(Duration.ofHours(1)))
                .claims(change);
        SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256");
        return NimbusJwtEncoder.withSecretKey(key).build()
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
                .getTokenValue();
    }

    @Test
    void theBaseTokenOfTheClaimCasesIsValid() {
        assertThat(service.isTokenValid(signedAccessToken(claims -> { }), TokenType.ACCESS)).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tokensWithBadClaims")
    void rejectsBadClaims(String description, Consumer<Map<String, Object>> change) {
        assertThat(service.isTokenValid(signedAccessToken(change), TokenType.ACCESS)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void acceptsEveryRoleOfTheDomain(Role role) {
        assertThat(service.isTokenValid(accessToken(role), TokenType.ACCESS)).isTrue();
    }

    // ------------------------------------------------------------ validation: malformed input

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-a-jwt", "a.b.c", "   "})
    void somethingThatIsNotATokenIsSimplyInvalid(String token) {
        assertThat(service.isTokenValid(token, TokenType.ACCESS)).isFalse();
        assertThat(service.isTokenValid(token, TokenType.REFRESH)).isFalse();
    }

    // ------------------------------------------------------------ key

    @Test
    void failsOnASecretThatIsEmptyOrNotBase64() {
        Clock clock = Clock.fixed(NOW, ZoneId.of("America/Bogota"));

        assertThatThrownBy(() -> new JwtService("", clock)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService("change-me!", clock)).isInstanceOf(IllegalArgumentException.class);
    }
}
