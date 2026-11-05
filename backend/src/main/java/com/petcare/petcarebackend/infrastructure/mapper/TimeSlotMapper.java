package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;

public final class TimeSlotMapper {

    private TimeSlotMapper() {
    }

    public static TimeSlotJpa toJpa(TimeSlot slot, OfferingJpa offering) {
        return new TimeSlotJpa(
                slot.getId(),
                offering,
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }

    public static TimeSlot toDomain(TimeSlotJpa entity) {
        return new TimeSlot(
                entity.getId(),
                entity.getOffering().getId(),
                entity.getSlotDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus()
        );
    }
}
