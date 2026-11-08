package com.petcare.petcarebackend.infrastructure.configuration;

import com.password4j.Argon2Function;
import com.password4j.types.Argon2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/registry/owner").permitAll()
                        // TODO: require authentication once JWT exists; ownerId currently comes from the request body
                        .requestMatchers(HttpMethod.POST, "/registry/pet").permitAll()
                        .requestMatchers(HttpMethod.POST, "/registry/provider").permitAll()
                        // Public catalog for the prototype; mutations remain protected.
                        .requestMatchers(HttpMethod.GET, "/services", "/services/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/service-requests").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // OWASP minimum recommendation for Argon2id
        return new Argon2Password4jPasswordEncoder(Argon2Function.getInstance(
                19456,      // memory (KiB)
                2,          // iterations
                1,          // parallelism
                32,         // hash length (bytes)
                Argon2.ID
        ));
    }
}
