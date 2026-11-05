package com.petcare.petcarebackend.application.exception;

public class PetNotOwnedException extends RuntimeException {
    public PetNotOwnedException() {
        super("The pet belongs to another owner");
    }
}
