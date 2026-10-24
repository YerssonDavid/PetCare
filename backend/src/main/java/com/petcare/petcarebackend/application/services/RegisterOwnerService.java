package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.application.port.in.OwnerRegistry;
import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterOwnerService implements OwnerRegistry {

    private final PasswordEncoder passwordEncoder;
    private final OwnerRepositoryPort ownerRepositoryPort;

    @Override
    public ResponseHttp registerOwner(RegisterOwnerRequest request) {
        Email email = new Email(request.email());
        Phone phone = new Phone(request.phone());

        if(ownerRepositoryPort.existsByEmail(email.value())){
            return new ResponseHttp(
                    "Error en el registro",
                    false,
                    LocalDateTime.now()
            );
        }

        // Implement security hash password
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create user
        Owner owner = Owner.register(request.name(), email, phone, hashedPassword);

        // Validate if exist and save user
        return ownerRepositoryPort.saveOwner(owner);
    }
}
