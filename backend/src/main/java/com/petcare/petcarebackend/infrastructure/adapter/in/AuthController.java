package com.petcare.petcarebackend.infrastructure.adapter.in;

import com.petcare.petcarebackend.application.dto.LoginOwnerRequest;
import com.petcare.petcarebackend.application.dto.LoginOwnerResponse;
import com.petcare.petcarebackend.application.services.LoginOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginOwnerService loginOwnerService;

    @PostMapping("/login")
    public ResponseEntity<LoginOwnerResponse> login(@Valid @RequestBody LoginOwnerRequest request) {
        return ResponseEntity.ok(loginOwnerService.login(request));
    }
}
