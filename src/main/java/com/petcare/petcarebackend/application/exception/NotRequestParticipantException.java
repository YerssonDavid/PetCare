package com.petcare.petcarebackend.application.exception;

public class NotRequestParticipantException extends RuntimeException {
    public NotRequestParticipantException() {
        super("The request belongs to other accounts");
    }
}
