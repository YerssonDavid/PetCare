package com.petcare.petcarebackend.application.dto;

import com.petcare.petcarebackend.domain.entity.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
    public static TimeSlotResponse from(TimeSlot slot) {
        return new TimeSlotResponse(
                slot.getId(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime()
        );
    }
}
