package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

// For a service it is the appointment time; for a product, the pickup or delivery time
public class TimeSlot {

    private static final LocalTime LATEST_END_TIME = LocalTime.of(17, 0);

    private UUID id;
    private UUID offeringId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private SlotStatus status;

    public TimeSlot() {
    }

    public TimeSlot(UUID id, UUID offeringId, LocalDate date, LocalTime startTime, LocalTime endTime, SlotStatus status) {
        this.id = id;
        this.offeringId = offeringId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Create factory; "now" comes from the caller so the domain does not depend on the system clock
    public static TimeSlot create(UUID offeringId, LocalDate date, LocalTime startTime, LocalTime endTime, LocalDateTime now) {
        if (date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Date, start time and end time are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (endTime.isAfter(LATEST_END_TIME)) {
            throw new IllegalArgumentException("Time slots must end by " + LATEST_END_TIME);
        }
        if (!date.atTime(startTime).isAfter(now)) {
            throw new IllegalArgumentException("Time slots must start in the future");
        }
        return new TimeSlot(UUID.randomUUID(), offeringId, date, startTime, endTime, SlotStatus.LIBRE);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(UUID offeringId) {
        this.offeringId = offeringId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }
}
