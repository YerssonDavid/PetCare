package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.exception.InvalidCredentialsException;
import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.domain.valueobject.Email;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService implements LoginUser {

    private final LoginUsersPort loginUsersPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    // Compared against when the email has no account, so an unknown email takes as long as a wrong
    // password and the response time does not reveal which emails are registered
    private final String dummyHash;

    public LoginService(LoginUsersPort loginUsersPort, PasswordEncoder passwordEncoder, AuthTokenIssuer authTokenIssuer) {
        this.loginUsersPort = loginUsersPort;
        this.passwordEncoder = passwordEncoder;
        this.authTokenIssuer = authTokenIssuer;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public LoginResponse loginUsers(LoginRequest login) {
        Email email = new Email(login.email());
        Optional<LoginAccount> account = loginUsersPort.findByEmail(email.value());

        // Always runs, even without an account, so every failure costs one BCrypt check
        boolean passwordMatches = passwordEncoder.matches(
                login.password(), account.map(LoginAccount::passwordHash).orElse(dummyHash));

        if (account.isEmpty() || !passwordMatches || !account.get().active()) {
            throw new InvalidCredentialsException();
        }

        return authTokenIssuer.issue(account.get());
    }
}
