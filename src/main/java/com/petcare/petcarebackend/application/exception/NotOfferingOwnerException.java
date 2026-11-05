package com.petcare.petcarebackend.application.exception;

public class NotOfferingOwnerException extends RuntimeException {
    public NotOfferingOwnerException() {
        super("El servicio o producto pertenece a otro proveedor");
    }
}
