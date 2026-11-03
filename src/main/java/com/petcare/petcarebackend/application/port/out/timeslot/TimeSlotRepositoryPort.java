package com.petcare.petcarebackend.application.port.out.timeslot;

import com.petcare.petcarebackend.domain.entity.TimeSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepositoryPort {
    void saveTimeSlot(TimeSlot slot);
    Optional<TimeSlot> findById(UUID id);
    // Atomic: returns false if the slot was already occupied, so only one concurrent caller can win
    boolean occupyIfFree(UUID id);
    void release(UUID id);
    boolean existsOverlappingServiceSlot(UUID providerId, LocalDate date, LocalTime startTime, LocalTime endTime);
    List<TimeSlot> findFreeByOfferingStartingAfter(UUID offeringId, LocalDateTime dateTime);
}
