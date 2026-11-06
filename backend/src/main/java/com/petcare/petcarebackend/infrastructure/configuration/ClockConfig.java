package com.petcare.petcarebackend.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    // Fixed zone: business dates and times must not depend on where the server runs
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
