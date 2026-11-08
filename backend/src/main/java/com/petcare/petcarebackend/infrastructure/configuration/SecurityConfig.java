package com.petcare.petcarebackend.infrastructure.configuration;

import com.petcare.petcarebackend.infrastructure.exception.SecurityErrorHandler;
import com.petcare.petcarebackend.infrastructure.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(
            pathPattern(HttpMethod.POST, "/auth/login"),
            pathPattern(HttpMethod.POST, "/auth/refresh"),
            pathPattern(HttpMethod.POST, "/registry/owner"),
            pathPattern(HttpMethod.POST, "/registry/provider"),
            pathPattern("/v3/api-docs/**"),
            pathPattern("/swagger-ui/**"),
            pathPattern("/swagger-ui.html")
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityErrorHandler errorHandler,
                                                   JwtService jwtService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/registry/pet", "/service-requests").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/pets", "/service-requests/mine").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/services", "/services/*/slots").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/service-requests/received", "/service-requests/provided").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.PATCH, "/service-requests/*/accept", "/service-requests/*/reject").hasRole("PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/services", "/services/*", "/services/*/slots/free", "/service-requests/*/history")
                        .hasAnyRole("USER", "PROVIDER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(server -> server
                        .jwt(jwt -> jwt.decoder(jwtService).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenResolver(ignoringPublicEndpoints())
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${FRONTEND_URL:http://localhost:5173}") String frontendUrl) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(JwtService.ROLE_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    // A client may attach its expired access token to every call; that must not block login or the refresh itself
    private BearerTokenResolver ignoringPublicEndpoints() {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return request -> PUBLIC_ENDPOINTS.matches(request) ? null : resolver.resolve(request);
    }
}
