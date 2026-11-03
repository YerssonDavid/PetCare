package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OfferingRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.TimeSlotRepository;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TimeSlotJpaAdapter implements TimeSlotRepositoryPort {

    private final TimeSlotRepository timeSlotRepository;
    private final OfferingRepository offeringRepository;

    @Override
    public void saveTimeSlot(TimeSlot slot) {
        // Proxy without SELECT: the use case already verified the offering exists
        OfferingJpa offering = offeringRepository.getReferenceById(slot.getOfferingId());
        timeSlotRepository.save(TimeSlotMapper.toJpa(slot, offering));
    }

    @Override
    public boolean existsOverlappingServiceSlot(UUID providerId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        return timeSlotRepository.existsOverlappingForProviderAndType(providerId, OfferingType.SERVICIO, date, startTime, endTime);
    }

    @Override
    public List<TimeSlot> findFreeByOfferingStartingAfter(UUID offeringId, LocalDateTime dateTime) {
        return timeSlotRepository.findByOfferingAndStatusStartingAfter(
                        offeringId, SlotStatus.LIBRE, dateTime.toLocalDate(), dateTime.toLocalTime()).stream()
                .map(TimeSlotMapper::toDomain)
                .toList();
    }
}
