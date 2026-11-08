package com.petcare.petcarebackend.application.exception;

public class TimeSlotOverlapException extends RuntimeException {
    public TimeSlotOverlapException() {
        super("The time slot overlaps another time slot of the provider");
    }
}
