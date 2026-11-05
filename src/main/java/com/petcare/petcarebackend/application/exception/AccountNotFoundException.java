package com.petcare.petcarebackend.application.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("No existe una cuenta con ese correo");
    }
}
