package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.in.query.ReceivedServiceRequestQuery;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListReceivedServiceRequestsService implements ReceivedServiceRequestQuery {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final ProviderRepositoryPort providerRepository;

    @Override
    public List<ReceivedServiceRequestResponse> listReceivedServiceRequests(UUID providerId, RequestStatus status) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (provider.getRole() != Role.PROVIDER) {
            throw new RoleNotAllowedException("Only providers can list received requests");
        }

        return serviceRequestRepository.findDetailsByProvider(providerId, status).stream()
                .map(ReceivedServiceRequestResponse::from)
                .toList();
    }
}
