package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.OfferingDetailResponse;
import com.petcare.petcarebackend.application.dto.TimeSlotResponse;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import com.petcare.petcarebackend.domain.valueobject.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class GetOfferingDetailServiceTest {

    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    // 2026-11-10 at 08:00 in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime NOW_IN_BOGOTA = LocalDateTime.of(2026, 11, 10, 8, 0);

    @Mock
    private OfferingRepositoryPort offeringRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    private GetOfferingDetailService service;

    @BeforeEach
    void setUp() {
        service = new GetOfferingDetailService(offeringRepository, providerRepository, timeSlotRepository, CLOCK);
    }

    private Offering offering() {
        return new Offering(OFFERING_ID, PROVIDER_ID, new OfferingName("Baño completo"), "Baño y secado", "Vacunas al día",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, OfferingType.SERVICIO, null);
    }

    private Provider provider() {
        return new Provider(PROVIDER_ID, new CommercialName("Vet Sol"), TypeProvider.CLINICA, "hashed", "description",
                new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 5, "Calle 1", Role.PROVIDER);
    }

    @Test
    void combinesOfferingProviderAndFreeSlots() {
        TimeSlot slot = new TimeSlot(UUID.randomUUID(), OFFERING_ID, LocalDate.of(2026, 11, 11),
                LocalTime.of(10, 0), LocalTime.of(11, 0), SlotStatus.LIBRE);
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering()));
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider()));
        when(timeSlotRepository.findFreeByOfferingStartingAfter(OFFERING_ID, NOW_IN_BOGOTA)).thenReturn(List.of(slot));

        OfferingDetailResponse detail = service.getOfferingDetail(OFFERING_ID);

        assertThat(detail.id()).isEqualTo(OFFERING_ID);
        assertThat(detail.name()).isEqualTo("Baño completo");
        assertThat(detail.description()).isEqualTo("Baño y secado");
        assertThat(detail.conditions()).isEqualTo("Vacunas al día");
        assertThat(detail.price()).isEqualByComparingTo("45000");
        assertThat(detail.category()).isEqualTo(OfferingCategory.PELUQUERIA);
        assertThat(detail.type()).isEqualTo(OfferingType.SERVICIO);
        assertThat(detail.imageUrl()).isNull();
        assertThat(detail.providerId()).isEqualTo(PROVIDER_ID);
        assertThat(detail.providerName()).isEqualTo("Vet Sol");
        assertThat(detail.providerPhone()).isEqualTo("3111111111");
        assertThat(detail.freeSlots()).containsExactly(TimeSlotResponse.from(slot));
    }

    @Test
    void returnsDetailWithoutFreeSlots() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering()));
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider()));
        when(timeSlotRepository.findFreeByOfferingStartingAfter(OFFERING_ID, NOW_IN_BOGOTA)).thenReturn(List.of());

        assertThat(service.getOfferingDetail(OFFERING_ID).freeSlots()).isEmpty();
    }

    @Test
    void rejectsUnknownOffering() {
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOfferingDetail(OFFERING_ID))
                .isInstanceOf(OfferingNotFoundException.class);
        verify(timeSlotRepository, never()).findFreeByOfferingStartingAfter(any(), any());
    }
}
