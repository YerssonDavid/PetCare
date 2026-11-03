package com.petcare.petcarebackend.application.port.out.timeslot;

import com.petcare.petcarebackend.domain.entity.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TimeSlotRepositoryPort {
    void saveTimeSlot(TimeSlot slot);
    boolean existsOverlappingServiceSlot(UUID providerId, LocalDate date, LocalTime startTime, LocalTime endTime);
    List<TimeSlot> findFreeByOfferingStartingAfter(UUID offeringId, LocalDateTime dateTime);
}
