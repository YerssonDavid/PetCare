package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.application.dto.ServiceRequestHistoryResponse;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.NotRequestParticipantException;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
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
class ServiceRequestQueriesTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PET_ID = UUID.randomUUID();
    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final UUID SLOT_ID = UUID.randomUUID();
    // 2026-11-10 at 08:00 in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 8, 0);

    @Mock
    private ServiceRequestRepositoryPort serviceRequestRepository;

    @Mock
    private ServiceRequestHistoryRepositoryPort historyRepository;

    @Mock
    private OwnerRepositoryPort ownerRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private ListOwnerServiceRequestsService listOwner;
    private ListProvidedServiceRequestsService listProvided;
    private GetServiceRequestHistoryService getHistory;

    @BeforeEach
    void setUp() {
        listOwner = new ListOwnerServiceRequestsService(serviceRequestRepository, ownerRepository);
        listProvided = new ListProvidedServiceRequestsService(serviceRequestRepository, providerRepository, CLOCK);
        getHistory = new GetServiceRequestHistoryService(serviceRequestRepository, historyRepository);
    }

    private Owner owner(Role role) {
        return new Owner(OWNER_ID, "Ana", new Email("ana@mail.com"), new Phone("3222222222"), "hashed",
                role, true, LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    private Provider provider(Role role) {
        return new Provider(PROVIDER_ID, new CommercialName("Vet Sol"), TypeProvider.CLINICA, "hashed", "d",
                new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 5, "Calle 1", role);
    }

    private ServiceRequestDetails details(RequestStatus status) {
        ServiceRequest request = new ServiceRequest(REQUEST_ID, OWNER_ID, PET_ID, OFFERING_ID, PROVIDER_ID, SLOT_ID,
                new Price(new BigDecimal("45000")), status, null, null, NOW.minusDays(2));
        Pet pet = new Pet(PET_ID, OWNER_ID, new PetName("Firulais"), TypePet.PERRO, 3, "Juguetón");
        Offering offering = new Offering(OFFERING_ID, PROVIDER_ID, new OfferingName("Baño completo"), "Baño y secado", "c",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, OfferingType.SERVICIO, null);
        TimeSlot slot = new TimeSlot(SLOT_ID, OFFERING_ID, LocalDate.of(2026, 11, 9),
                LocalTime.of(10, 0), LocalTime.of(11, 0), SlotStatus.OCUPADA);
        return new ServiceRequestDetails(request, owner(Role.USER), pet, offering, provider(Role.PROVIDER), slot);
    }

    @Test
    void listsTheRequestsOfTheOwner() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.USER)));
        when(serviceRequestRepository.findDetailsByOwner(OWNER_ID)).thenReturn(List.of(details(RequestStatus.ACEPTADO)));

        List<ServiceRequestResponse> responses = listOwner.listOwnerServiceRequests(OWNER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(REQUEST_ID);
        assertThat(responses.get(0).status()).isEqualTo(RequestStatus.ACEPTADO);
        assertThat(responses.get(0).requestedAt()).isEqualTo(NOW.minusDays(2));
    }

    @Test
    void ownerListRejectsAccountWithoutUserRole() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.PROVIDER)));

        assertThatThrownBy(() -> listOwner.listOwnerServiceRequests(OWNER_ID))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(serviceRequestRepository, never()).findDetailsByOwner(any());
    }

    @Test
    void ownerListRejectsUnknownOwner() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listOwner.listOwnerServiceRequests(OWNER_ID))
                .isInstanceOf(OwnerNotFoundException.class);
    }

    @Test
    void listsProvidedServicesAsOfTheCurrentTimeWithDescriptionAndPet() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(Role.PROVIDER)));
        when(serviceRequestRepository.findAcceptedServiceDetailsStartedBy(PROVIDER_ID, NOW))
                .thenReturn(List.of(details(RequestStatus.ACEPTADO)));

        List<ReceivedServiceRequestResponse> responses = listProvided.listProvidedServiceRequests(PROVIDER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).offeringDescription()).isEqualTo("Baño y secado");
        assertThat(responses.get(0).pet().name()).isEqualTo("Firulais");
        assertThat(responses.get(0).ownerEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    void providedListRejectsAccountWithoutProviderRole() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(Role.USER)));

        assertThatThrownBy(() -> listProvided.listProvidedServiceRequests(PROVIDER_ID))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(serviceRequestRepository, never()).findAcceptedServiceDetailsStartedBy(any(), any());
    }

    @Test
    void providedListRejectsUnknownProvider() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listProvided.listProvidedServiceRequests(PROVIDER_ID))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    private void givenRejectedRequestWithHistory() {
        when(serviceRequestRepository.findDetailsById(REQUEST_ID)).thenReturn(Optional.of(details(RequestStatus.RECHAZADO)));
        when(historyRepository.findByRequestId(REQUEST_ID)).thenReturn(List.of(
                new ServiceRequestHistory(UUID.randomUUID(), REQUEST_ID, OWNER_ID, RequestStatus.RECHAZADO, "Sin disponibilidad", NOW),
                new ServiceRequestHistory(UUID.randomUUID(), REQUEST_ID, OWNER_ID, RequestStatus.PENDIENTE, null, NOW.minusDays(2))));
    }

    private void assertFullHistory(ServiceRequestHistoryResponse response) {
        assertThat(response.requestId()).isEqualTo(REQUEST_ID);
        assertThat(response.offeringName()).isEqualTo("Baño completo");
        assertThat(response.offeringDescription()).isEqualTo("Baño y secado");
        assertThat(response.pet().name()).isEqualTo("Firulais");
        assertThat(response.pet().typePet()).isEqualTo(TypePet.PERRO);
        assertThat(response.entries()).containsExactly(
                new ServiceRequestHistoryResponse.Entry(RequestStatus.RECHAZADO, "Sin disponibilidad", NOW),
                new ServiceRequestHistoryResponse.Entry(RequestStatus.PENDIENTE, null, NOW.minusDays(2)));
    }

    @Test
    void ownerGetsTheHistory() {
        givenRejectedRequestWithHistory();

        assertFullHistory(getHistory.getServiceRequestHistory(REQUEST_ID, OWNER_ID));
    }

    @Test
    void providerGetsTheHistory() {
        givenRejectedRequestWithHistory();

        assertFullHistory(getHistory.getServiceRequestHistory(REQUEST_ID, PROVIDER_ID));
    }

    @Test
    void otherAccountsCannotGetTheHistory() {
        when(serviceRequestRepository.findDetailsById(REQUEST_ID)).thenReturn(Optional.of(details(RequestStatus.PENDIENTE)));

        assertThatThrownBy(() -> getHistory.getServiceRequestHistory(REQUEST_ID, UUID.randomUUID()))
                .isInstanceOf(NotRequestParticipantException.class);
        verify(historyRepository, never()).findByRequestId(any());
    }

    @Test
    void historyRejectsUnknownRequest() {
        when(serviceRequestRepository.findDetailsById(REQUEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getHistory.getServiceRequestHistory(REQUEST_ID, OWNER_ID))
                .isInstanceOf(ServiceRequestNotFoundException.class);
        verify(historyRepository, never()).findByRequestId(any());
    }
}
