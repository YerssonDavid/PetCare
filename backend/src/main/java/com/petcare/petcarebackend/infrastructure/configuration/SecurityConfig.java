package com.petcare.petcarebackend.infrastructure.configuration;

import com.password4j.Argon2Function;
import com.password4j.types.Argon2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/registry/owner").permitAll()
                        // TODO: require authentication once JWT exists; ownerId currently comes from the request body
                        .requestMatchers(HttpMethod.POST, "/registry/pet").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
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
