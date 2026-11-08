package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.TimeSlotResponse;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFreeTimeSlotsServiceTest {

    private static final UUID OFFERING_ID = UUID.randomUUID();
    // 03:00 UTC on the 11th is still 22:00 on the 10th in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-11T03:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime NOW_IN_BOGOTA = LocalDateTime.of(2026, 11, 10, 22, 0);

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    @Mock
    private OfferingRepositoryPort offeringRepository;

    private ListFreeTimeSlotsService service;

    @BeforeEach
    void setUp() {
        service = new ListFreeTimeSlotsService(timeSlotRepository, offeringRepository, CLOCK);
    }

    @Test
    void returnsFreeSlotsStartingAfterNowInBogota() {
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), OFFERING_ID, LocalDate.of(2026, 11, 11),
                LocalTime.of(10, 0), LocalTime.of(11, 0), SlotStatus.LIBRE);
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(new Offering()));
        when(timeSlotRepository.findFreeByOfferingStartingAfter(OFFERING_ID, NOW_IN_BOGOTA)).thenReturn(List.of(slot));

        List<TimeSlotResponse> slots = service.listFreeTimeSlots(OFFERING_ID);

        assertThat(slots).containsExactly(new TimeSlotResponse(
                slot.getId(), LocalDate.of(2026, 11, 11), LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    void returnsEmptyListWhenThereAreNoFreeSlots() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(new Offering()));
        when(timeSlotRepository.findFreeByOfferingStartingAfter(OFFERING_ID, NOW_IN_BOGOTA)).thenReturn(List.of());

        assertThat(service.listFreeTimeSlots(OFFERING_ID)).isEmpty();
    }

    @Test
    void rejectsUnknownOffering() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listFreeTimeSlots(OFFERING_ID))
                .isInstanceOf(OfferingNotFoundException.class);
        verify(timeSlotRepository, never()).findFreeByOfferingStartingAfter(any(), any());
    }
}
