package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOfferingsServiceTest {

    @Mock
    private OfferingRepositoryPort offeringRepository;

    @InjectMocks
    private ListOfferingsService service;

    private Offering offering() {
        return Offering.publish(UUID.randomUUID(), new OfferingName("Paseo"), "Paseo de una hora", null,
                new Price(new BigDecimal("20000")), OfferingCategory.PASEO, OfferingType.SERVICIO);
    }

    @Test
    void filtersByCategoryWhenGiven() {
        Offering offering = offering();
        when(offeringRepository.findByCategory(OfferingCategory.PASEO)).thenReturn(List.of(offering));

        List<OfferingResponse> offerings = service.listOfferings(OfferingCategory.PASEO);

        assertThat(offerings).containsExactly(OfferingResponse.from(offering));
        verify(offeringRepository, never()).findAll();
    }

    @Test
    void listsEverythingWithoutCategory() {
        Offering offering = offering();
        when(offeringRepository.findAll()).thenReturn(List.of(offering));

        List<OfferingResponse> offerings = service.listOfferings(null);

        assertThat(offerings).containsExactly(OfferingResponse.from(offering));
        verify(offeringRepository, never()).findByCategory(any());
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        when(offeringRepository.findByCategory(OfferingCategory.VETERINARIA)).thenReturn(List.of());

        assertThat(service.listOfferings(OfferingCategory.VETERINARIA)).isEmpty();
    }
}
