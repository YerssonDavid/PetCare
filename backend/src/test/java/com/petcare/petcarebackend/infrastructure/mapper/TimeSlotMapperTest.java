package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSlotMapperTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 10);
    private static final LocalTime START = LocalTime.of(10, 0);
    private static final LocalTime END = LocalTime.of(11, 0);

    private TimeSlot slot(UUID offeringId) {
        return new TimeSlot(UUID.randomUUID(), offeringId, DATE, START, END, SlotStatus.OCUPADA);
    }

    private OfferingJpa offering(UUID id) {
        OfferingJpa offering = new OfferingJpa();
        offering.setId(id);
        return offering;
    }

    @Test
    void toJpaCopiesEveryField() {
        UUID offeringId = UUID.randomUUID();
        OfferingJpa offering = offering(offeringId);
        TimeSlot slot = slot(offeringId);

        TimeSlotJpa entity = TimeSlotMapper.toJpa(slot, offering);

        assertThat(entity.getId()).isEqualTo(slot.getId());
        assertThat(entity.getOffering()).isSameAs(offering);
        assertThat(entity.getSlotDate()).isEqualTo(DATE);
        assertThat(entity.getStartTime()).isEqualTo(START);
        assertThat(entity.getEndTime()).isEqualTo(END);
        assertThat(entity.getStatus()).isEqualTo(SlotStatus.OCUPADA);
    }

    @Test
    void toDomainRebuildsTheSameSlot() {
        UUID offeringId = UUID.randomUUID();
        TimeSlot original = slot(offeringId);

        TimeSlot restored = TimeSlotMapper.toDomain(TimeSlotMapper.toJpa(original, offering(offeringId)));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
