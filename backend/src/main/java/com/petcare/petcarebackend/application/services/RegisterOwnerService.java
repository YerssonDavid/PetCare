package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OwnerAlreadyExistsException;
import com.petcare.petcarebackend.application.port.in.registry.OwnerRegistry;
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
    public RegisterResponse registerOwner(RegisterOwnerRequest request) {
        Email email = new Email(request.email());
        Phone phone = new Phone(request.phone());

        if(ownerRepositoryPort.existsByEmail(email.value())){
            throw new OwnerAlreadyExistsException();
        }

        // Implement security hash password
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create user
        Owner owner = Owner.register(request.name(), email, phone, hashedPassword);

        ownerRepositoryPort.saveOwner(owner);

        return new RegisterResponse(
                owner.getId(),
                "El usuario se ha registrado correctamente",
                LocalDateTime.now()
        );
    }
}
