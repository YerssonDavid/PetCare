package com.petcare.petcarebackend.application.exception;

public class TimeSlotOverlapException extends RuntimeException {
    public TimeSlotOverlapException() {
        super("La franja se cruza con otra franja del proveedor");
    }
}
