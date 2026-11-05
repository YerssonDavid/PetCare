package com.petcare.petcarebackend.application.exception;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("La sesión no es válida o ya expiró; inicia sesión de nuevo");
    }
}
