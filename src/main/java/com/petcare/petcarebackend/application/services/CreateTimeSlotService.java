package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.CreateTimeSlotRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.NotOfferingOwnerException;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.TimeSlotOverlapException;
import com.petcare.petcarebackend.application.port.in.registry.TimeSlotRegistry;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTimeSlotService implements TimeSlotRegistry {

    private final TimeSlotRepositoryPort timeSlotRepository;
    private final OfferingRepositoryPort offeringRepository;
    private final ProviderRepositoryPort providerRepository;
    private final Clock clock;

    @Override
    @Transactional
    public RegisterResponse createTimeSlot(UUID offeringId, CreateTimeSlotRequest request) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new OfferingNotFoundException(offeringId));

        Provider provider = providerRepository.findByIdForUpdate(request.providerId())
                .orElseThrow(() -> new ProviderNotFoundException(request.providerId()));

        if (provider.getRole() != Role.PROVIDER) {
            throw new RoleNotAllowedException("Only providers can create time slots");
        }
        if (!offering.getProviderId().equals(provider.getId())) {
            throw new NotOfferingOwnerException();
        }

        TimeSlot slot = TimeSlot.create(
                offeringId,
                request.date(),
                request.startTime(),
                request.endTime(),
                LocalDateTime.now(clock)
        );

        if (timeSlotRepository.existsOverlappingForProvider(provider.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime())) {
            throw new TimeSlotOverlapException();
        }

        timeSlotRepository.saveTimeSlot(slot);
        return new RegisterResponse(slot.getId(), "La franja horaria se ha creado correctamente", LocalDateTime.now(clock));
    }
}
