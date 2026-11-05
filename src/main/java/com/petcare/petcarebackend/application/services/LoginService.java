package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;
import com.petcare.petcarebackend.application.exception.AccountNotFoundException;
import com.petcare.petcarebackend.application.exception.InvalidCredentialsException;
import com.petcare.petcarebackend.application.port.in.login.LoginUser;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUser {

    private final LoginUsersPort loginUsersPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenIssuer authTokenIssuer;

    @Override
    public LoginResponse loginUsers(LoginRequest login) {
        Email email = new Email(login.email());
        LoginAccount account = loginUsersPort.findByEmail(email.value())
                .orElseThrow(AccountNotFoundException::new);

        if (!passwordEncoder.matches(login.password(), account.passwordHash()) || !account.active()) {
            throw new InvalidCredentialsException();
        }

        return authTokenIssuer.issue(account);
    }
}
