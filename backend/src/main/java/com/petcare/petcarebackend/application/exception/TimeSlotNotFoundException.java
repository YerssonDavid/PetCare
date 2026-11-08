package com.petcare.petcarebackend.application.exception;

import java.util.UUID;

public class TimeSlotNotFoundException extends RuntimeException {
    public TimeSlotNotFoundException(UUID slotId) {
        super("Franja horaria no encontrada: " + slotId);
    }
}
