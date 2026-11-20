package com.petcare.petcarebackend.application.port.out.login;

import com.petcare.petcarebackend.application.dto.LoginAccount;

import java.util.Optional;
import java.util.UUID;

public interface LoginUsersPort {
    // Looks in owners first and then in providers; empty if the email is in neither
    Optional<LoginAccount> findByEmail(String email);
    // Same order: owners first and then providers
    Optional<LoginAccount> findById(UUID id);
}
