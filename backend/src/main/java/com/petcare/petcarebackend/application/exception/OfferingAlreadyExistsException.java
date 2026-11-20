package com.petcare.petcarebackend.application.exception;

public class OfferingAlreadyExistsException extends RuntimeException {
    public OfferingAlreadyExistsException(String name) {
        super("El proveedor ya tiene un servicio o producto llamado '" + name + "'");
    }
}
