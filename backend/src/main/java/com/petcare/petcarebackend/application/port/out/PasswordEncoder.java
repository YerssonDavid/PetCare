package com.petcare.petcarebackend.application.port.out;

public interface PasswordEncoder {
    String encode(String password);
}
