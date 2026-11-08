package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OfferingAlreadyExistsException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishOfferingServiceTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();

    @Mock
    private OfferingRepositoryPort offeringRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    @InjectMocks
    private PublishOfferingService service;

    private PublishOfferingRequest request(String price) {
        return new PublishOfferingRequest(PROVIDER_ID, "Baño completo", "Baño y secado", "Vacunas al día",
                new BigDecimal(price), OfferingCategory.PELUQUERIA, OfferingType.SERVICIO);
    }

    private Provider providerWithRole(Role role) {
        Provider provider = new Provider();
        provider.setId(PROVIDER_ID);
        provider.setRole(role);
        return provider;
    }

    @Test
    void savesOfferingLinkedToProviderAndReturnsItsId() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.PROVIDER)));
        when(offeringRepository.existsByProviderIdAndNormalizedName(PROVIDER_ID, "baño completo")).thenReturn(false);

        RegisterResponse response = service.publishOffering(request("45000"));

        ArgumentCaptor<Offering> saved = ArgumentCaptor.forClass(Offering.class);
        verify(offeringRepository).saveOffering(saved.capture());
        Offering offering = saved.getValue();
        assertThat(offering.getProviderId()).isEqualTo(PROVIDER_ID);
        assertThat(offering.getName().value()).isEqualTo("Baño completo");
        assertThat(offering.getDescription()).isEqualTo("Baño y secado");
        assertThat(offering.getConditions()).isEqualTo("Vacunas al día");
        assertThat(offering.getPrice().value()).isEqualByComparingTo("45000");
        assertThat(offering.getCategory()).isEqualTo(OfferingCategory.PELUQUERIA);
        assertThat(offering.getType()).isEqualTo(OfferingType.SERVICIO);
        assertThat(offering.getImageUrl()).isNull();
        assertThat(response.id()).isNotNull().isEqualTo(offering.getId());
    }

    @Test
    void rejectsNameTheProviderAlreadyUses() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.PROVIDER)));
        when(offeringRepository.existsByProviderIdAndNormalizedName(PROVIDER_ID, "baño completo")).thenReturn(true);

        assertThatThrownBy(() -> service.publishOffering(request("45000")))
                .isInstanceOf(OfferingAlreadyExistsException.class);
        verify(offeringRepository, never()).saveOffering(any());
    }

    @Test
    void rejectsUnknownProvider() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publishOffering(request("45000")))
                .isInstanceOf(ProviderNotFoundException.class);
        verify(offeringRepository, never()).saveOffering(any());
    }

    @Test
    void rejectsAccountWithoutProviderRole() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(providerWithRole(Role.USER)));

        assertThatThrownBy(() -> service.publishOffering(request("45000")))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(offeringRepository, never()).saveOffering(any());
    }

    @Test
    void rejectsInvalidPriceBeforeTouchingRepository() {
        assertThatThrownBy(() -> service.publishOffering(request("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(providerRepository, never()).findById(any());
        verify(offeringRepository, never()).saveOffering(any());
    }
}
