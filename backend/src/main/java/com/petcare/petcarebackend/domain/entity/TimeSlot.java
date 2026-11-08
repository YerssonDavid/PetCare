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
            throw new IllegalArgumentException("La fecha, la hora de inicio y la hora de fin son obligatorias");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }
        if (endTime.isAfter(LATEST_END_TIME)) {
            throw new IllegalArgumentException("Las franjas deben terminar a más tardar a las " + LATEST_END_TIME);
        }
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), offeringId, date, startTime, endTime, SlotStatus.LIBRE);
        if (slot.hasStarted(now)) {
            throw new IllegalArgumentException("Las franjas deben empezar en el futuro");
        }
        return slot;
    }

    public boolean hasStarted(LocalDateTime now) {
        return !date.atTime(startTime).isAfter(now);
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
