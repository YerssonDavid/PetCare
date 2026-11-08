package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OfferingAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.in.registry.OfferingRegistry;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PublishOfferingService implements OfferingRegistry {

    private final OfferingRepositoryPort offeringRepository;
    private final ProviderRepositoryPort providerRepository;

    @Override
    public RegisterResponse publishOffering(PublishOfferingRequest request) {
        OfferingName name = new OfferingName(request.name());
        Price price = new Price(request.price());

        Provider provider = providerRepository.findById(request.providerId())
                .orElseThrow(() -> new ProviderNotFoundException(request.providerId()));

        if (provider.getRole() != Role.PROVIDER) {
            throw new RoleNotAllowedException("Only providers can publish services");
        }

        if (offeringRepository.existsByProviderIdAndNormalizedName(provider.getId(), name.normalized())) {
            throw new OfferingAlreadyExistsException(name.value());
        }

        Offering offering = Offering.publish(
                provider.getId(),
                name,
                request.description(),
                request.conditions(),
                price,
                request.category(),
                request.type()
        );

        offeringRepository.saveOffering(offering);
        return new RegisterResponse(offering.getId(), "El servicio se ha publicado correctamente", LocalDateTime.now());
    }
}
