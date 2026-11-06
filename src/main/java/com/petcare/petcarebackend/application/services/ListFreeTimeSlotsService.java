package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.TimeSlotResponse;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.port.in.query.TimeSlotQuery;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListFreeTimeSlotsService implements TimeSlotQuery {

    private final TimeSlotRepositoryPort timeSlotRepository;
    private final OfferingRepositoryPort offeringRepository;
    private final Clock clock;

    @Override
    public List<TimeSlotResponse> listFreeTimeSlots(UUID offeringId) {
        if (offeringRepository.findById(offeringId).isEmpty()) {
            throw new OfferingNotFoundException(offeringId);
        }

        return timeSlotRepository.findFreeByOfferingStartingAfter(offeringId, LocalDateTime.now(clock)).stream()
                .map(TimeSlotResponse::from)
                .toList();
    }
}
