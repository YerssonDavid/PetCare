package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.LoginOwnerRequest;
import com.petcare.petcarebackend.application.dto.LoginOwnerResponse;
import com.petcare.petcarebackend.application.exception.InvalidOwnerCredentialsException;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginOwnerService {

    private final OwnerRepositoryPort ownerRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginOwnerResponse login(LoginOwnerRequest request) {
        String email = new Email(request.email()).value();
        Owner owner = ownerRepository.findByEmail(email)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActiveAccount()))
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(InvalidOwnerCredentialsException::new);

        return new LoginOwnerResponse(owner.getId(), owner.getName(), owner.getEmail().value());
    }
}
