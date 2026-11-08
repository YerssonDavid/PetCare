package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.DuplicateServiceRequestException;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotOwnedException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.TimeSlotNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestRegistry;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.pet.PetRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateServiceRequestService implements ServiceRequestRegistry {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final ServiceRequestHistoryRepositoryPort historyRepository;
    private final OwnerRepositoryPort ownerRepository;
    private final PetRepositoryPort petRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final OfferingRepositoryPort offeringRepository;
    private final ProviderRepositoryPort providerRepository;
    private final Clock clock;

    // Any failure rolls everything back, so a slot is never left occupied without its request
    @Override
    @Transactional
    public ServiceRequestResponse createServiceRequest(CreateServiceRequestRequest request) {
        Owner owner = ownerRepository.findById(request.ownerId())
                .orElseThrow(() -> new OwnerNotFoundException(request.ownerId()));

        if (owner.getRole() != Role.USER) {
            throw new RoleNotAllowedException("El propietario no tiene permiso para solicitar servicios");
        }

        // The lock makes concurrent requests of one pet run one at a time, so two overlapping
        // requests on slots of different providers cannot both pass the duplicate check
        Pet pet = petRepository.findByIdForUpdate(request.petId())
                .orElseThrow(() -> new PetNotFoundException(request.petId()));

        if (!pet.getOwnerId().equals(owner.getId())) {
            throw new PetNotOwnedException();
        }

        TimeSlot slot = timeSlotRepository.findById(request.slotId())
                .orElseThrow(() -> new TimeSlotNotFoundException(request.slotId()));

        LocalDateTime now = LocalDateTime.now(clock);
        if (slot.hasStarted(now)) {
            throw new TimeSlotUnavailableException("La franja ya no está disponible");
        }

        Offering offering = offeringRepository.findById(slot.getOfferingId())
                .orElseThrow(() -> new OfferingNotFoundException(slot.getOfferingId()));
        Provider provider = providerRepository.findById(offering.getProviderId())
                .orElseThrow(() -> new ProviderNotFoundException(offering.getProviderId()));

        ServiceRequest serviceRequest = ServiceRequest.create(
                owner.getId(), pet.getId(), offering, slot, request.purchaseType(), now);

        if (offering.getType() == OfferingType.SERVICIO) {
            if (serviceRequestRepository.existsActiveServiceRequestOverlapping(
                    pet.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime())) {
                throw new DuplicateServiceRequestException();
            }
            if (!timeSlotRepository.occupyIfFree(slot.getId())) {
                throw new TimeSlotUnavailableException("La franja ya no está disponible");
            }
        } else if (serviceRequestRepository.existsActiveRequestForPetAndSlot(pet.getId(), slot.getId())) {
            // A slot belongs to one product, so this only stops repeating the same product;
            // products never occupy their slot nor block the pet's schedule
            throw new DuplicateServiceRequestException();
        }

        serviceRequestRepository.saveServiceRequest(serviceRequest);
        historyRepository.saveHistory(ServiceRequestHistory.of(serviceRequest, now));

        return ServiceRequestResponse.from(serviceRequest, pet, offering, provider, slot);
    }
}
