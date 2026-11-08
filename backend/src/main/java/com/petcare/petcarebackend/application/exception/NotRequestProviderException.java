package com.petcare.petcarebackend.application.exception;

public class NotRequestProviderException extends RuntimeException {
    public NotRequestProviderException() {
        super("La solicitud pertenece a otro proveedor");
    }
}
