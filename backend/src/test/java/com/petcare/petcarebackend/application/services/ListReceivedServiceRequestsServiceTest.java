package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import com.petcare.petcarebackend.domain.valueobject.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
class ListReceivedServiceRequestsServiceTest {

    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PET_ID = UUID.randomUUID();
    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID SLOT_ID = UUID.randomUUID();

    @Mock
    private ServiceRequestRepositoryPort serviceRequestRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private ListReceivedServiceRequestsService service;

    @BeforeEach
    void setUp() {
        service = new ListReceivedServiceRequestsService(serviceRequestRepository, providerRepository);
    }

    private Provider provider(Role role) {
        return new Provider(PROVIDER_ID, new CommercialName("Vet Sol"), TypeProvider.CLINICA, "hashed", "d",
                new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 5, "Calle 1", role);
    }

    private ServiceRequestDetails details() {
        ServiceRequest request = new ServiceRequest(UUID.randomUUID(), OWNER_ID, PET_ID, OFFERING_ID, PROVIDER_ID, SLOT_ID,
                new Price(new BigDecimal("45000")), RequestStatus.PENDIENTE, null, null, LocalDateTime.of(2026, 11, 10, 8, 0));
        Owner owner = new Owner(OWNER_ID, "Ana", new Email("ana@mail.com"), new Phone("3222222222"), "hashed",
                Role.USER, true, LocalDateTime.of(2026, 1, 1, 0, 0));
        Pet pet = new Pet(PET_ID, OWNER_ID, new PetName("Firulais"), TypePet.PERRO, 3, "Juguetón");
        Offering offering = new Offering(OFFERING_ID, PROVIDER_ID, new OfferingName("Baño completo"), "d", "c",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, OfferingType.SERVICIO, null);
        TimeSlot slot = new TimeSlot(SLOT_ID, OFFERING_ID, LocalDate.of(2026, 11, 11),
                LocalTime.of(10, 0), LocalTime.of(11, 0), SlotStatus.OCUPADA);
        return new ServiceRequestDetails(request, owner, pet, offering, provider(Role.PROVIDER), slot);
    }

    @Test
    void returnsPetDataAndClientEmail() {
        ServiceRequestDetails details = details();
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(Role.PROVIDER)));
        when(serviceRequestRepository.findDetailsByProvider(PROVIDER_ID, RequestStatus.PENDIENTE)).thenReturn(List.of(details));

        List<ReceivedServiceRequestResponse> responses =
                service.listReceivedServiceRequests(PROVIDER_ID, RequestStatus.PENDIENTE);

        assertThat(responses).hasSize(1);
        ReceivedServiceRequestResponse response = responses.get(0);
        assertThat(response.id()).isEqualTo(details.request().getId());
        assertThat(response.ownerEmail()).isEqualTo("ana@mail.com");
        assertThat(response.pet().id()).isEqualTo(PET_ID);
        assertThat(response.pet().name()).isEqualTo("Firulais");
        assertThat(response.pet().typePet()).isEqualTo(TypePet.PERRO);
        assertThat(response.pet().age()).isEqualTo(3);
        assertThat(response.pet().description()).isEqualTo("Juguetón");
        assertThat(response.offeringName()).isEqualTo("Baño completo");
        assertThat(response.offeringDescription()).isEqualTo("d");
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 11, 11));
        assertThat(response.price()).isEqualByComparingTo("45000");
        assertThat(response.status()).isEqualTo(RequestStatus.PENDIENTE);
    }

    @Test
    void withoutStatusAsksForEveryStatus() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(Role.PROVIDER)));
        when(serviceRequestRepository.findDetailsByProvider(PROVIDER_ID, null)).thenReturn(List.of());

        assertThat(service.listReceivedServiceRequests(PROVIDER_ID, null)).isEmpty();
    }

    @Test
    void rejectsAccountWithoutProviderRole() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(Role.USER)));

        assertThatThrownBy(() -> service.listReceivedServiceRequests(PROVIDER_ID, null))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(serviceRequestRepository, never()).findDetailsByProvider(any(), any());
    }

    @Test
    void rejectsUnknownProvider() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listReceivedServiceRequests(PROVIDER_ID, null))
                .isInstanceOf(ProviderNotFoundException.class);
        verify(serviceRequestRepository, never()).findDetailsByProvider(any(), any());
    }
}
