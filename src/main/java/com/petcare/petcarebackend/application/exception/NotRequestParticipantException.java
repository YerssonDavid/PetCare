package com.petcare.petcarebackend.application.exception;

public class NotRequestParticipantException extends RuntimeException {
    public NotRequestParticipantException() {
        super("La solicitud pertenece a otras cuentas");
    }
}
