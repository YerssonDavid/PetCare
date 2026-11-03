package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.SlotStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeSlotTest {

    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);
    private static final LocalDateTime NOW = TODAY.atTime(9, 30);

    private TimeSlot create(LocalDate date, String start, String end) {
        return TimeSlot.create(OFFERING_ID, date, LocalTime.parse(start), LocalTime.parse(end), NOW);
    }

    private void assertRejected(LocalDate date, String start, String end) {
        assertThatThrownBy(() -> create(date, start, end))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createsFreeSlot() {
        TimeSlot slot = create(TOMORROW, "10:00", "11:00");

        assertThat(slot.getId()).isNotNull();
        assertThat(slot.getOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(slot.getDate()).isEqualTo(TOMORROW);
        assertThat(slot.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(slot.getEndTime()).isEqualTo(LocalTime.of(11, 0));
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.LIBRE);
    }

    @Test
    void acceptsSlotEndingExactlyAtFivePm() {
        assertThat(create(TOMORROW, "16:00", "17:00").getEndTime()).isEqualTo(LocalTime.of(17, 0));
    }

    @Test
    void rejectsSlotEndingAfterFivePm() {
        assertRejected(TOMORROW, "16:00", "17:01");
    }

    @Test
    void rejectsEndTimeBeforeStartTime() {
        assertRejected(TOMORROW, "11:00", "10:00");
    }

    @Test
    void rejectsEndTimeEqualToStartTime() {
        assertRejected(TOMORROW, "10:00", "10:00");
    }

    @Test
    void acceptsLaterTimeToday() {
        assertThat(create(TODAY, "09:31", "10:00").getDate()).isEqualTo(TODAY);
    }

    @Test
    void rejectsSlotStartingNow() {
        assertRejected(TODAY, "09:30", "10:00");
    }

    @Test
    void rejectsEarlierTimeToday() {
        assertRejected(TODAY, "09:00", "10:00");
    }

    @Test
    void rejectsPastDate() {
        assertRejected(TODAY.minusDays(1), "10:00", "11:00");
    }

    @Test
    void rejectsMissingValues() {
        assertThatThrownBy(() -> TimeSlot.create(OFFERING_ID, null, LocalTime.NOON, LocalTime.of(13, 0), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TimeSlot.create(OFFERING_ID, TOMORROW, null, LocalTime.of(13, 0), NOW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TimeSlot.create(OFFERING_ID, TOMORROW, LocalTime.NOON, null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
