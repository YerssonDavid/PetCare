package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
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
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;
import com.petcare.petcarebackend.infrastructure.mapper.OfferingMapper;
import com.petcare.petcarebackend.infrastructure.mapper.OwnerMapper;
import com.petcare.petcarebackend.infrastructure.mapper.PetMapper;
import com.petcare.petcarebackend.infrastructure.mapper.ProviderMapper;
import com.petcare.petcarebackend.infrastructure.mapper.ServiceRequestMapper;
import com.petcare.petcarebackend.infrastructure.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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

    @Override
    public boolean existsActiveRequestForPetAndSlot(UUID petId, UUID slotId) {
        return serviceRequestRepository.existsByPetIdAndSlotIdAndStatusIn(petId, slotId, ACTIVE_STATUSES);
    }

    @Override
    public Optional<ServiceRequestDetails> findDetailsById(UUID id) {
        return serviceRequestRepository.findDetailsById(id).map(this::toDetails);
    }

    @Override
    public List<ServiceRequestDetails> findDetailsByProvider(UUID providerId, RequestStatus status) {
        List<RequestStatus> statuses = status == null ? List.of(RequestStatus.values()) : List.of(status);
        return serviceRequestRepository.findByProviderIdAndStatusInOrderByRequestedAtDesc(providerId, statuses).stream()
                .map(this::toDetails)
                .toList();
    }

    @Override
    public List<ServiceRequestDetails> findDetailsByOwner(UUID ownerId) {
        return serviceRequestRepository.findByOwnerIdOrderBySlotDesc(ownerId).stream()
                .map(this::toDetails)
                .toList();
    }

    @Override
    public List<ServiceRequestDetails> findAcceptedServiceDetailsStartedBy(UUID providerId, LocalDateTime now) {
        return serviceRequestRepository.findByProviderStatusAndTypeStartedBy(
                        providerId, RequestStatus.ACEPTADO, OfferingType.SERVICIO, now.toLocalDate(), now.toLocalTime()).stream()
                .map(this::toDetails)
                .toList();
    }

    @Override
    public boolean updateIfPending(ServiceRequest request) {
        return serviceRequestRepository.updateAnswerIfCurrent(request.getId(), RequestStatus.PENDIENTE,
                request.getStatus(), request.getRejectionReason()) == 1;
    }

    private ServiceRequestDetails toDetails(ServiceRequestJpa entity) {
        return new ServiceRequestDetails(
                ServiceRequestMapper.toDomain(entity),
                OwnerMapper.toDomain(entity.getOwner()),
                PetMapper.toDomain(entity.getPet()),
                OfferingMapper.toDomain(entity.getOffering()),
                ProviderMapper.toDomain(entity.getProvider()),
                TimeSlotMapper.toDomain(entity.getSlot())
        );
    }
}
