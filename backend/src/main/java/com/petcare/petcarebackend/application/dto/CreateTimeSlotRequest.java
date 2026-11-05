package com.petcare.petcarebackend.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateTimeSlotRequest(
        // TODO: remove once JWT exists; the provider ID must come from the token
        @NotNull
        UUID providerId,

        @NotNull
        LocalDate date,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime
) {
}
