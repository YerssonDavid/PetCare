package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterProviderRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.ProviderAlreadyExistsException;
import com.petcare.petcarebackend.application.port.in.registry.ProviderRegistry;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.ProviderRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegisterProviderService implements ProviderRegistry {

    private final ProviderRepositoryPort providerRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse registerProvider(RegisterProviderRequest request) {

        CommercialName commercialName = new CommercialName(request.nameCommercial());
        Email email = new Email(request.email());
        Phone phone = new Phone(request.phone());

        if(providerRepositoryPort.existsByName(commercialName.value()) || providerRepositoryPort.existsByEmail(email.value()))
            throw new ProviderAlreadyExistsException("The provider already exists");

        String hashedPassword = passwordEncoder.encode(request.password());

        Provider provider = Provider.register(
                commercialName,
                request.typeProvider(),
                hashedPassword,
                request.description(),
                phone,
                email,
                request.city(),
                request.experienceYears(),
                request.address()
        );

        providerRepositoryPort.saveProvider(provider);
        return new RegisterResponse(provider.getId(), "El proveedor se ha registrado correctamente", LocalDateTime.now());
    }
}
