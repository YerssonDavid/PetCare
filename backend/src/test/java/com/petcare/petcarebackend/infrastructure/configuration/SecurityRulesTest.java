package com.petcare.petcarebackend.infrastructure.configuration;

import com.petcare.petcarebackend.application.dto.ClaimsUser;
import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.in.login.TokenRefresher;
import com.petcare.petcarebackend.application.port.in.query.OfferingDetailQuery;
import com.petcare.petcarebackend.application.port.in.query.OfferingQuery;
import com.petcare.petcarebackend.application.port.in.query.OwnerServiceRequestQuery;
import com.petcare.petcarebackend.application.port.in.query.PetQuery;
import com.petcare.petcarebackend.application.port.in.query.ProvidedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.in.query.ReceivedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.in.query.ServiceRequestHistoryQuery;
import com.petcare.petcarebackend.application.port.in.query.TimeSlotQuery;
import com.petcare.petcarebackend.application.port.in.registry.OfferingRegistry;
import com.petcare.petcarebackend.application.port.in.registry.OwnerRegistry;
import com.petcare.petcarebackend.application.port.in.registry.PetRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ProviderRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestAnswerRegistry;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
import com.petcare.petcarebackend.application.port.in.registry.TimeSlotRegistry;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.infrastructure.exception.SecurityErrorHandler;
import com.petcare.petcarebackend.infrastructure.services.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Goes through the real filter chain with real tokens; only the use cases behind the controllers are mocks
@WebMvcTest(properties = {
        "JWT_SECRET=" + SecurityRulesTest.SECRET,
        "FRONTEND_URL=" + SecurityRulesTest.FRONTEND
})
@Import({SecurityConfig.class, SecurityErrorHandler.class, JwtService.class, ClockConfig.class})
@MockitoBean(types = {
        LoginUser.class, TokenRefresher.class,
        OwnerRegistry.class, PetRegistry.class, ProviderRegistry.class,
        OfferingRegistry.class, OfferingQuery.class, OfferingDetailQuery.class,
        TimeSlotRegistry.class, TimeSlotQuery.class,
        ServiceRequestRegistry.class, ServiceRequestAnswerRegistry.class,
        ReceivedServiceRequestQuery.class, ProvidedServiceRequestQuery.class,
        OwnerServiceRequestQuery.class, ServiceRequestHistoryQuery.class,
        PetQuery.class
})
class SecurityRulesTest {

    // 32 bytes in Base64, only for this test
    static final String SECRET = "AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=";
    static final String FRONTEND = "https://app.petcare.test";

    private static final String OTHER_SECRET = "AgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgI=";
    private static final UUID ID = UUID.randomUUID();

    private static final List<Role> OWNER_ONLY = List.of(Role.USER);
    private static final List<Role> PROVIDER_ONLY = List.of(Role.PROVIDER);
    private static final List<Role> BOTH = List.of(Role.USER, Role.PROVIDER);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PetQuery petQuery;

    @Autowired
    private PetRegistry petRegistry;

    @Autowired
    private OfferingRegistry offeringRegistry;

    @Autowired
    private ServiceRequestRegistry serviceRequestRegistry;

    @Autowired
    private ServiceRequestAnswerRegistry serviceRequestAnswerRegistry;

    private static Stream<Arguments> protectedEndpoints() {
        return Stream.of(
                arguments(HttpMethod.POST, "/registry/pet", OWNER_ONLY),
                arguments(HttpMethod.GET, "/pets", OWNER_ONLY),
                arguments(HttpMethod.POST, "/service-requests", OWNER_ONLY),
                arguments(HttpMethod.GET, "/service-requests/mine", OWNER_ONLY),
                arguments(HttpMethod.POST, "/services", PROVIDER_ONLY),
                arguments(HttpMethod.POST, "/services/" + ID + "/slots", PROVIDER_ONLY),
                arguments(HttpMethod.GET, "/service-requests/received", PROVIDER_ONLY),
                arguments(HttpMethod.GET, "/service-requests/provided", PROVIDER_ONLY),
                arguments(HttpMethod.PATCH, "/service-requests/" + ID + "/accept", PROVIDER_ONLY),
                arguments(HttpMethod.PATCH, "/service-requests/" + ID + "/reject", PROVIDER_ONLY),
                arguments(HttpMethod.GET, "/services", BOTH),
                arguments(HttpMethod.GET, "/services/" + ID, BOTH),
                arguments(HttpMethod.GET, "/services/" + ID + "/slots/free", BOTH),
                arguments(HttpMethod.GET, "/service-requests/" + ID + "/history", BOTH)
        );
    }

    private static Stream<Arguments> publicEndpoints() {
        return Stream.of(
                arguments(HttpMethod.POST, "/auth/login"),
                arguments(HttpMethod.POST, "/auth/refresh"),
                arguments(HttpMethod.POST, "/registry/owner"),
                arguments(HttpMethod.POST, "/registry/provider"),
                arguments(HttpMethod.GET, "/v3/api-docs"),
                arguments(HttpMethod.GET, "/swagger-ui/index.html"),
                arguments(HttpMethod.GET, "/swagger-ui.html")
        );
    }

    private static String tokenOf(UUID accountId, Role role) {
        return new JwtService(SECRET, Clock.systemUTC()).generateAccessToken(new ClaimsUser(accountId, role));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String expiredToken() {
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        return new JwtService(SECRET, twoHoursAgo).generateAccessToken(new ClaimsUser(ID, Role.USER));
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private MockHttpServletRequestBuilder call(HttpMethod method, String path) {
        return request(method, path).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    private int statusOf(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse().getStatus();
    }

    @ParameterizedTest
    @MethodSource("protectedEndpoints")
    void anonymousCallsAreRejected(HttpMethod method, String path) throws Exception {
        mockMvc.perform(call(method, path))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.detail").value("Debes iniciar sesión para acceder a este recurso"));
    }

    // 400 from an empty body or a null from a mock are fine here: they prove the call got past security
    @ParameterizedTest
    @MethodSource("protectedEndpoints")
    void allowedRolesGetPastSecurity(HttpMethod method, String path, List<Role> allowed) throws Exception {
        for (Role role : allowed) {
            int status = statusOf(call(method, path).header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ID, role))));

            assertThat(status).as("%s %s as %s", method, path, role).isNotIn(401, 403);
        }
    }

    @ParameterizedTest
    @MethodSource("protectedEndpoints")
    void otherRolesAreForbidden(HttpMethod method, String path, List<Role> allowed) throws Exception {
        for (Role role : Role.values()) {
            if (allowed.contains(role)) {
                continue;
            }
            mockMvc.perform(call(method, path).header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ID, role))))
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Acceso denegado"))
                    .andExpect(jsonPath("$.detail").value("No tienes permiso para realizar esta acción"));
        }
    }

    @ParameterizedTest
    @MethodSource("publicEndpoints")
    void publicEndpointsNeedNoToken(HttpMethod method, String path) throws Exception {
        assertThat(statusOf(call(method, path))).isNotIn(401, 403);
    }

    // A client may attach its expired token to every call, and that must not block the refresh that would fix it
    @ParameterizedTest
    @MethodSource("publicEndpoints")
    void publicEndpointsIgnoreAnExpiredToken(HttpMethod method, String path) throws Exception {
        assertThat(statusOf(call(method, path).header(HttpHeaders.AUTHORIZATION, bearer(expiredToken())))).isNotIn(401, 403);
    }

    private void assertTokenIsRejected(String token) throws Exception {
        mockMvc.perform(get("/services").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("No autenticado"))
                .andExpect(jsonPath("$.detail").value("El token de acceso no es válido o ya expiró"));
    }

    @Test
    void rejectsAnExpiredToken() throws Exception {
        assertTokenIsRejected(expiredToken());
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() throws Exception {
        assertTokenIsRejected(new JwtService(OTHER_SECRET, Clock.systemUTC())
                .generateAccessToken(new ClaimsUser(ID, Role.USER)));
    }

    @Test
    void rejectsATokenWhosePayloadWasChangedToGainARole() throws Exception {
        String[] parts = tokenOf(ID, Role.USER).split("\\.");
        String forgedPayload = base64Url("{\"sub\":\"%s\",\"role\":\"PROVIDER\",\"exp\":%d}"
                .formatted(ID, Instant.now().plusSeconds(3600).getEpochSecond()));

        assertTokenIsRejected(parts[0] + "." + forgedPayload + "." + parts[2]);
    }

    @Test
    void rejectsAnUnsignedToken() throws Exception {
        String payload = base64Url("{\"sub\":\"%s\",\"role\":\"USER\",\"exp\":%d}"
                .formatted(ID, Instant.now().plusSeconds(3600).getEpochSecond()));

        assertTokenIsRejected(base64Url("{\"alg\":\"none\"}") + "." + payload + ".");
    }

    // Signed with the right key and not expired, but its type does not open protected routes
    @Test
    void rejectsARefreshTokenUsedAsAnAccessToken() throws Exception {
        assertTokenIsRejected(new JwtService(SECRET, Clock.systemUTC()).generateRefreshToken(ID).token());
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-jwt", "AbCdEfGhIjKlMnOpQrStUvWxYz0123456789_-AbCdE"})
    void rejectsSomethingThatIsNotAJwt(String token) throws Exception {
        assertTokenIsRejected(token);
    }

    @Test
    void anUnknownRouteIsNotFoundOnceAuthenticated() throws Exception {
        mockMvc.perform(get("/does-not-exist").header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ID, Role.USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("La ruta solicitada no existe"));
    }

    @Test
    void anUnknownRouteAsksForAuthenticationFirst() throws Exception {
        mockMvc.perform(get("/does-not-exist")).andExpect(status().isUnauthorized());
    }

    // The account always comes from the token, whatever the request claims
    @Test
    void listingPetsIgnoresAnOwnerIdInTheQuery() throws Exception {
        UUID ownerId = UUID.randomUUID();

        mockMvc.perform(get("/pets").param("ownerId", UUID.randomUUID().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ownerId, Role.USER))))
                .andExpect(status().isOk());

        verify(petQuery).listPetsByOwner(ownerId);
    }

    @Test
    void registeringAPetIgnoresAnOwnerIdInTheBody() throws Exception {
        UUID ownerId = UUID.randomUUID();

        mockMvc.perform(post("/registry/pet").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ownerId, Role.USER)))
                        .content("""
                                {"ownerId":"%s","name":"Luna","typePet":"PERRO","age":3}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        verify(petRegistry).registerPet(eq(ownerId), any());
    }

    @Test
    void publishingAServiceIgnoresAProviderIdInTheBody() throws Exception {
        UUID providerId = UUID.randomUUID();

        mockMvc.perform(post("/services").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(providerId, Role.PROVIDER)))
                        .content("""
                                {"providerId":"%s","name":"Baño","description":"Baño y secado","price":10,"category":"PASEO","type":"SERVICIO"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());

        verify(offeringRegistry).publishOffering(eq(providerId), any());
    }

    @Test
    void requestingAServiceIgnoresAnOwnerIdInTheBody() throws Exception {
        UUID ownerId = UUID.randomUUID();

        mockMvc.perform(post("/service-requests").contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(ownerId, Role.USER)))
                        .content("""
                                {"ownerId":"%s","petId":"%s","slotId":"%s"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated());

        verify(serviceRequestRegistry).createServiceRequest(eq(ownerId), any());
    }

    @Test
    void acceptingNeedsNoBodyAndUsesTheProviderOfTheToken() throws Exception {
        UUID providerId = UUID.randomUUID();

        mockMvc.perform(patch("/service-requests/{id}/accept", ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(providerId, Role.PROVIDER))))
                .andExpect(status().isOk());

        verify(serviceRequestAnswerRegistry).acceptServiceRequest(ID, providerId);
    }

    @Test
    void anOwnerCannotAnswerARequest() throws Exception {
        mockMvc.perform(patch("/service-requests/{id}/accept", ID)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenOf(UUID.randomUUID(), Role.USER))))
                .andExpect(status().isForbidden());

        verify(serviceRequestAnswerRegistry, never()).acceptServiceRequest(any(), any());
    }

    @Test
    void corsAllowsTheFrontend() throws Exception {
        mockMvc.perform(options("/pets")
                        .header(HttpHeaders.ORIGIN, FRONTEND)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, FRONTEND));
    }

    @Test
    void corsRejectsAnyOtherOrigin() throws Exception {
        mockMvc.perform(options("/pets")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
