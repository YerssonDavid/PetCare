package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.in.query.OwnerServiceRequestQuery;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListOwnerServiceRequestsService implements OwnerServiceRequestQuery {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final OwnerRepositoryPort ownerRepository;

    @Override
    public List<ServiceRequestResponse> listOwnerServiceRequests(UUID ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new OwnerNotFoundException(ownerId));

        if (owner.getRole() != Role.USER) {
            throw new RoleNotAllowedException("Only owners can list their requests");
        }

        return serviceRequestRepository.findDetailsByOwner(ownerId).stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }
}
