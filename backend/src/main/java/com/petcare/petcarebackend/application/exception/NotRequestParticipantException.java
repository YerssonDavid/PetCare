package com.petcare.petcarebackend.application.exception;

public class NotRequestParticipantException extends RuntimeException {
    public NotRequestParticipantException() {
        super("No tienes permiso para modificar esta solicitud");
    }
}
