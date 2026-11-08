package com.petcare.petcarebackend.infrastructure.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Keeps databases created by an earlier prototype version compatible with the
 * current request lifecycle, which includes the CANCELADA status.
 */
@Configuration
public class DatabaseCompatibilityMigration {

    @Bean
    CommandLineRunner updateServiceRequestStatusConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("ALTER TABLE service_requests DROP CONSTRAINT IF EXISTS service_requests_status_check");
            jdbcTemplate.execute("ALTER TABLE service_requests ADD CONSTRAINT service_requests_status_check "
                    + "CHECK (status IN ('PENDIENTE', 'ACEPTADO', 'RECHAZADO', 'CANCELADA'))");
            jdbcTemplate.execute("ALTER TABLE service_request_history DROP CONSTRAINT IF EXISTS service_request_history_status_check");
            jdbcTemplate.execute("ALTER TABLE service_request_history ADD CONSTRAINT service_request_history_status_check "
                    + "CHECK (status IN ('PENDIENTE', 'ACEPTADO', 'RECHAZADO', 'CANCELADA'))");
        };
    }
}
