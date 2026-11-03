package com.petcare.petcarebackend.domain.exception;

public class RequestNotPendingException extends RuntimeException {
    public RequestNotPendingException() {
        super("The request has already been answered");
    }
}
