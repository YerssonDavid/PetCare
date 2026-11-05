package com.petcare.petcarebackend.domain.exception;

public class RequestNotPendingException extends RuntimeException {
    public RequestNotPendingException() {
        super("La solicitud ya fue respondida");
    }
}
