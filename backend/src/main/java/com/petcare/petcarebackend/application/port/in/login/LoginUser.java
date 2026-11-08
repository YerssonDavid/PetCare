package com.petcare.petcarebackend.application.port.in.login;

import com.petcare.petcarebackend.application.dto.LoginRequest;
import com.petcare.petcarebackend.application.dto.LoginResponse;

public interface LoginUser {
    LoginResponse loginUsers(LoginRequest login);
}
