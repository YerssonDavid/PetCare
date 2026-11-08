package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.in.query.ProvidedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProvidedServiceRequestsService implements ProvidedServiceRequestQuery {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final ProviderRepositoryPort providerRepository;
    private final Clock clock;

    @Override
    public List<ReceivedServiceRequestResponse> listProvidedServiceRequests(UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (provider.getRole() != Role.PROVIDER) {
            throw new RoleNotAllowedException("Only providers can list provided requests");
        }

        return serviceRequestRepository.findAcceptedServiceDetailsStartedBy(providerId, LocalDateTime.now(clock)).stream()
                .map(ReceivedServiceRequestResponse::from)
                .toList();
    }
}
