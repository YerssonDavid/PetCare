package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OfferingRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.PetRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ServiceRequestRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.TimeSlotRepository;
import com.petcare.petcarebackend.infrastructure.mapper.ServiceRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceRequestJpaAdapter implements ServiceRequestRepositoryPort {

    private static final List<RequestStatus> ACTIVE_STATUSES = List.of(RequestStatus.PENDIENTE, RequestStatus.ACEPTADO);

    private final ServiceRequestRepository serviceRequestRepository;
    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final OfferingRepository offeringRepository;
    private final ProviderRepository providerRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Override
    public void saveServiceRequest(ServiceRequest request) {
        // Proxies without SELECT: the use case already verified that all of them exist
        serviceRequestRepository.saveAndFlush(ServiceRequestMapper.toJpa(
                request,
                ownerRepository.getReferenceById(request.getOwnerId()),
                petRepository.getReferenceById(request.getPetId()),
                offeringRepository.getReferenceById(request.getOfferingId()),
                providerRepository.getReferenceById(request.getProviderId()),
                timeSlotRepository.getReferenceById(request.getSlotId())
        ));
    }

    @Override
    public boolean existsActiveServiceRequestOverlapping(UUID petId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return serviceRequestRepository.existsOverlapping(
                petId, ACTIVE_STATUSES, OfferingType.SERVICIO, date, startTime, endTime);
    }
}
