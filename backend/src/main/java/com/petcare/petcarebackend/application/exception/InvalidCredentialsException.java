package com.petcare.petcarebackend.application.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }
}
