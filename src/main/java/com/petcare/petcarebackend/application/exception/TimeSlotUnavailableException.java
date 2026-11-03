package com.petcare.petcarebackend.application.exception;

public class TimeSlotUnavailableException extends RuntimeException {
    public TimeSlotUnavailableException(String message) {
        super(message);
    }
}
