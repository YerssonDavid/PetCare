package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.CreateTimeSlotRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.NotOfferingOwnerException;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.TimeSlotOverlapException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTimeSlotServiceTest {

    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    // 2026-11-10 at 08:00 in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), BOGOTA);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 11, 11);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime ELEVEN = LocalTime.of(11, 0);

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    @Mock
    private OfferingRepositoryPort offeringRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private CreateTimeSlotService service;

    @BeforeEach
    void setUp() {
        service = new CreateTimeSlotService(timeSlotRepository, offeringRepository, providerRepository, CLOCK);
    }

    private CreateTimeSlotRequest request(LocalDate date) {
        return new CreateTimeSlotRequest(date, TEN, ELEVEN);
    }

    private Offering offeringOwnedBy(UUID providerId) {
        return offeringOfType(providerId, OfferingType.SERVICIO);
    }

    private Offering offeringOfType(UUID providerId, OfferingType type) {
        Offering offering = new Offering();
        offering.setId(OFFERING_ID);
        offering.setProviderId(providerId);
        offering.setType(type);
        return offering;
    }

    private Provider providerWithRole(Role role) {
        Provider provider = new Provider();
        provider.setId(PROVIDER_ID);
        provider.setRole(role);
        return provider;
    }

    private void givenOwnerProvider() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offeringOwnedBy(PROVIDER_ID)));
        when(providerRepository.findByIdForUpdate(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.PROVIDER)));
    }

    @Test
    void savesFreeSlotAndReturnsItsId() {
        givenOwnerProvider();
        when(timeSlotRepository.existsOverlappingServiceSlot(PROVIDER_ID, TOMORROW, TEN, ELEVEN)).thenReturn(false);

        RegisterResponse response = service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW));

        ArgumentCaptor<TimeSlot> saved = ArgumentCaptor.forClass(TimeSlot.class);
        verify(timeSlotRepository).saveTimeSlot(saved.capture());
        TimeSlot slot = saved.getValue();
        assertThat(slot.getOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(slot.getDate()).isEqualTo(TOMORROW);
        assertThat(slot.getStartTime()).isEqualTo(TEN);
        assertThat(slot.getEndTime()).isEqualTo(ELEVEN);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.LIBRE);
        assertThat(response.id()).isNotNull().isEqualTo(slot.getId());
    }

    @Test
    void locksProviderBeforeCheckingOverlaps() {
        givenOwnerProvider();
        when(timeSlotRepository.existsOverlappingServiceSlot(PROVIDER_ID, TOMORROW, TEN, ELEVEN)).thenReturn(false);

        service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW));

        InOrder order = inOrder(providerRepository, timeSlotRepository);
        order.verify(providerRepository).findByIdForUpdate(PROVIDER_ID);
        order.verify(timeSlotRepository).existsOverlappingServiceSlot(PROVIDER_ID, TOMORROW, TEN, ELEVEN);
        order.verify(timeSlotRepository).saveTimeSlot(any());
    }

    @Test
    void rejectsOverlappingSlot() {
        givenOwnerProvider();
        when(timeSlotRepository.existsOverlappingServiceSlot(PROVIDER_ID, TOMORROW, TEN, ELEVEN)).thenReturn(true);

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW)))
                .isInstanceOf(TimeSlotOverlapException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }

    @Test
    void savesProductSlotWithoutCheckingOverlaps() {
        when(offeringRepository.findById(OFFERING_ID))
                .thenReturn(Optional.of(offeringOfType(PROVIDER_ID, OfferingType.PRODUCTO)));
        when(providerRepository.findByIdForUpdate(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.PROVIDER)));

        service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW));

        verify(timeSlotRepository, never()).existsOverlappingServiceSlot(any(), any(), any(), any());
        verify(timeSlotRepository).saveTimeSlot(any());
    }

    @Test
    void usesTheClockToRejectPastSlots() {
        givenOwnerProvider();

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(LocalDate.of(2026, 11, 9))))
                .isInstanceOf(IllegalArgumentException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }

    @Test
    void rejectsUnknownOffering() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW)))
                .isInstanceOf(OfferingNotFoundException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }

    @Test
    void rejectsUnknownProvider() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offeringOwnedBy(PROVIDER_ID)));
        when(providerRepository.findByIdForUpdate(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW)))
                .isInstanceOf(ProviderNotFoundException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }

    @Test
    void rejectsAccountWithoutProviderRole() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offeringOwnedBy(PROVIDER_ID)));
        when(providerRepository.findByIdForUpdate(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.USER)));

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW)))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }

    @Test
    void rejectsOfferingOfAnotherProvider() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offeringOwnedBy(UUID.randomUUID())));
        when(providerRepository.findByIdForUpdate(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.PROVIDER)));

        assertThatThrownBy(() -> service.createTimeSlot(OFFERING_ID, PROVIDER_ID, request(TOMORROW)))
                .isInstanceOf(NotOfferingOwnerException.class);
        verify(timeSlotRepository, never()).saveTimeSlot(any());
    }
}
