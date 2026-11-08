package com.petcare.petcarebackend.application.exception;

public class InvalidOwnerCredentialsException extends RuntimeException {
    public InvalidOwnerCredentialsException() {
        super("El correo o la contraseña no son correctos");
    }
}
