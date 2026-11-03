package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.CreateServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.DuplicateServiceRequestException;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotFoundException;
import com.petcare.petcarebackend.application.exception.PetNotOwnedException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.TimeSlotNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.pet.PetRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.domain.entity.TimeSlot;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.PurchaseType;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class CreateServiceRequestServiceTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PET_ID = UUID.randomUUID();
    private static final UUID SLOT_ID = UUID.randomUUID();
    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    // 2026-11-10 at 08:00 in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 8, 0);
    private static final LocalDate DATE = LocalDate.of(2026, 11, 11);
    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime ELEVEN = LocalTime.of(11, 0);

    @Mock
    private ServiceRequestRepositoryPort serviceRequestRepository;

    @Mock
    private ServiceRequestHistoryRepositoryPort historyRepository;

    @Mock
    private OwnerRepositoryPort ownerRepository;

    @Mock
    private PetRepositoryPort petRepository;

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    @Mock
    private OfferingRepositoryPort offeringRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private CreateServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new CreateServiceRequestService(serviceRequestRepository, historyRepository, ownerRepository,
                petRepository, timeSlotRepository, offeringRepository, providerRepository, CLOCK);
    }

    private CreateServiceRequestRequest request(PurchaseType purchaseType) {
        return new CreateServiceRequestRequest(OWNER_ID, PET_ID, SLOT_ID, purchaseType);
    }

    private Owner owner(Role role) {
        Owner owner = new Owner();
        owner.setId(OWNER_ID);
        owner.setRole(role);
        return owner;
    }

    private Pet petOwnedBy(UUID ownerId) {
        return new Pet(PET_ID, ownerId, new PetName("Firulais"), TypePet.PERRO, 3, "d");
    }

    private TimeSlot slot(LocalDate date) {
        return new TimeSlot(SLOT_ID, OFFERING_ID, date, TEN, ELEVEN, SlotStatus.LIBRE);
    }

    private Offering offering(OfferingType type) {
        return new Offering(OFFERING_ID, PROVIDER_ID, new OfferingName("Baño completo"), "d", "c",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, type, null);
    }

    private Provider provider() {
        return new Provider(PROVIDER_ID, new CommercialName("Vet Sol"), TypeProvider.CLINICA, "hashed", "d",
                new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 5, "Calle 1", Role.PROVIDER);
    }

    private void givenOwnerAndPet() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.USER)));
        when(petRepository.findByIdForUpdate(PET_ID)).thenReturn(Optional.of(petOwnedBy(OWNER_ID)));
    }

    private void givenSlotOf(OfferingType type) {
        givenOwnerAndPet();
        when(timeSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot(DATE)));
        when(offeringRepository.findById(OFFERING_ID)).thenReturn(Optional.of(offering(type)));
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider()));
    }

    private void verifyNothingSaved() {
        verify(serviceRequestRepository, never()).saveServiceRequest(any());
        verify(historyRepository, never()).saveHistory(any());
    }

    @Test
    void serviceOccupiesSlotSavesRequestAndHistory() {
        givenSlotOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.existsActiveServiceRequestOverlapping(PET_ID, DATE, TEN, ELEVEN)).thenReturn(false);
        when(timeSlotRepository.occupyIfFree(SLOT_ID)).thenReturn(true);

        ServiceRequestResponse response = service.createServiceRequest(request(null));

        ArgumentCaptor<ServiceRequest> savedRequest = ArgumentCaptor.forClass(ServiceRequest.class);
        ArgumentCaptor<ServiceRequestHistory> savedHistory = ArgumentCaptor.forClass(ServiceRequestHistory.class);
        verify(serviceRequestRepository).saveServiceRequest(savedRequest.capture());
        verify(historyRepository).saveHistory(savedHistory.capture());

        ServiceRequest saved = savedRequest.getValue();
        assertThat(saved.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(saved.getPetId()).isEqualTo(PET_ID);
        assertThat(saved.getOfferingId()).isEqualTo(OFFERING_ID);
        assertThat(saved.getProviderId()).isEqualTo(PROVIDER_ID);
        assertThat(saved.getSlotId()).isEqualTo(SLOT_ID);
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
        assertThat(saved.getRequestedAt()).isEqualTo(NOW);

        assertThat(savedHistory.getValue().getRequestId()).isEqualTo(saved.getId());
        assertThat(savedHistory.getValue().getStatus()).isEqualTo(RequestStatus.PENDIENTE);

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.petName()).isEqualTo("Firulais");
        assertThat(response.offeringName()).isEqualTo("Baño completo");
        assertThat(response.offeringType()).isEqualTo(OfferingType.SERVICIO);
        assertThat(response.providerName()).isEqualTo("Vet Sol");
        assertThat(response.date()).isEqualTo(DATE);
        assertThat(response.startTime()).isEqualTo(TEN);
        assertThat(response.endTime()).isEqualTo(ELEVEN);
        assertThat(response.price()).isEqualByComparingTo("45000");
        assertThat(response.status()).isEqualTo(RequestStatus.PENDIENTE);
    }

    @Test
    void locksPetThenChecksDuplicatesThenOccupiesThenSaves() {
        givenSlotOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.existsActiveServiceRequestOverlapping(PET_ID, DATE, TEN, ELEVEN)).thenReturn(false);
        when(timeSlotRepository.occupyIfFree(SLOT_ID)).thenReturn(true);

        service.createServiceRequest(request(null));

        InOrder order = inOrder(petRepository, serviceRequestRepository, timeSlotRepository, historyRepository);
        order.verify(petRepository).findByIdForUpdate(PET_ID);
        order.verify(serviceRequestRepository).existsActiveServiceRequestOverlapping(PET_ID, DATE, TEN, ELEVEN);
        order.verify(timeSlotRepository).occupyIfFree(SLOT_ID);
        order.verify(serviceRequestRepository).saveServiceRequest(any());
        order.verify(historyRepository).saveHistory(any());
    }

    @Test
    void productNeitherOccupiesSlotNorChecksDuplicates() {
        givenSlotOf(OfferingType.PRODUCTO);

        ServiceRequestResponse response = service.createServiceRequest(request(PurchaseType.RETIRO));

        verify(timeSlotRepository, never()).occupyIfFree(any());
        verify(serviceRequestRepository, never()).existsActiveServiceRequestOverlapping(any(), any(), any(), any());
        verify(serviceRequestRepository).saveServiceRequest(any());
        verify(historyRepository).saveHistory(any());
        assertThat(response.purchaseType()).isEqualTo(PurchaseType.RETIRO);
    }

    @Test
    void rejectsPetWithOverlappingService() {
        givenSlotOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.existsActiveServiceRequestOverlapping(PET_ID, DATE, TEN, ELEVEN)).thenReturn(true);

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(DuplicateServiceRequestException.class);
        verify(timeSlotRepository, never()).occupyIfFree(any());
        verifyNothingSaved();
    }

    @Test
    void rejectsSlotAlreadyOccupied() {
        givenSlotOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.existsActiveServiceRequestOverlapping(PET_ID, DATE, TEN, ELEVEN)).thenReturn(false);
        when(timeSlotRepository.occupyIfFree(SLOT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(TimeSlotUnavailableException.class);
        verifyNothingSaved();
    }

    @Test
    void rejectsPurchaseTypeOnServiceBeforeOccupyingSlot() {
        givenSlotOf(OfferingType.SERVICIO);

        assertThatThrownBy(() -> service.createServiceRequest(request(PurchaseType.ENTREGA)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(timeSlotRepository, never()).occupyIfFree(any());
        verifyNothingSaved();
    }

    @Test
    void rejectsProductWithoutPurchaseType() {
        givenSlotOf(OfferingType.PRODUCTO);

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNothingSaved();
    }

    @Test
    void rejectsSlotThatAlreadyStarted() {
        givenOwnerAndPet();
        when(timeSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot(LocalDate.of(2026, 11, 9))));

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(TimeSlotUnavailableException.class);
        verify(timeSlotRepository, never()).occupyIfFree(any());
        verifyNothingSaved();
    }

    @Test
    void rejectsUnknownSlot() {
        givenOwnerAndPet();
        when(timeSlotRepository.findById(SLOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(TimeSlotNotFoundException.class);
        verifyNothingSaved();
    }

    @Test
    void rejectsPetOfAnotherOwner() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.USER)));
        when(petRepository.findByIdForUpdate(PET_ID)).thenReturn(Optional.of(petOwnedBy(UUID.randomUUID())));

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(PetNotOwnedException.class);
        verifyNothingSaved();
    }

    @Test
    void rejectsUnknownPet() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.USER)));
        when(petRepository.findByIdForUpdate(PET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(PetNotFoundException.class);
        verifyNothingSaved();
    }

    @Test
    void rejectsAccountWithoutUserRole() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner(Role.PROVIDER)));

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(petRepository, never()).findByIdForUpdate(any());
        verifyNothingSaved();
    }

    @Test
    void rejectsUnknownOwner() {
        when(ownerRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createServiceRequest(request(null)))
                .isInstanceOf(OwnerNotFoundException.class);
        verifyNothingSaved();
    }
}
