package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.AcceptServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.RejectServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.NotRequestProviderException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
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
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
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
import java.time.LocalDateTime;
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
class RespondServiceRequestServiceTest {

    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PET_ID = UUID.randomUUID();
    private static final UUID SLOT_ID = UUID.randomUUID();
    private static final UUID OFFERING_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final String REASON = "Sin disponibilidad";
    // 2026-11-10 at 08:00 in Bogota
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T13:00:00Z"), ZoneId.of("America/Bogota"));
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 8, 0);
    private static final LocalDateTime FUTURE_START = LocalDateTime.of(2026, 11, 11, 10, 0);

    @Mock
    private ServiceRequestRepositoryPort serviceRequestRepository;

    @Mock
    private ServiceRequestHistoryRepositoryPort historyRepository;

    @Mock
    private TimeSlotRepositoryPort timeSlotRepository;

    @Mock
    private ProviderRepositoryPort providerRepository;

    private RespondServiceRequestService service;

    @BeforeEach
    void setUp() {
        service = new RespondServiceRequestService(serviceRequestRepository, historyRepository, timeSlotRepository,
                providerRepository, CLOCK);
    }

    private Provider provider(UUID id, Role role) {
        return new Provider(id, new CommercialName("Vet Sol"), TypeProvider.CLINICA, "hashed", "d",
                new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 5, "Calle 1", role);
    }

    private ServiceRequestDetails details(OfferingType type, RequestStatus status) {
        return details(type, status, FUTURE_START);
    }

    private ServiceRequestDetails details(OfferingType type, RequestStatus status, LocalDateTime slotStart) {
        PurchaseType purchaseType = type == OfferingType.PRODUCTO ? PurchaseType.RETIRO : null;
        ServiceRequest request = new ServiceRequest(REQUEST_ID, OWNER_ID, PET_ID, OFFERING_ID, PROVIDER_ID, SLOT_ID,
                new Price(new BigDecimal("45000")), status, purchaseType, null, NOW.minusDays(1));
        Owner owner = new Owner();
        owner.setId(OWNER_ID);
        Pet pet = new Pet(PET_ID, OWNER_ID, new PetName("Firulais"), TypePet.PERRO, 3, "d");
        Offering offering = new Offering(OFFERING_ID, PROVIDER_ID, new OfferingName("Baño completo"), "d", "c",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, type, null);
        TimeSlot slot = new TimeSlot(SLOT_ID, OFFERING_ID, slotStart.toLocalDate(),
                slotStart.toLocalTime(), slotStart.toLocalTime().plusHours(1), SlotStatus.OCUPADA);
        return new ServiceRequestDetails(request, owner, pet, offering, provider(PROVIDER_ID, Role.PROVIDER), slot);
    }

    private void givenPendingRequestOf(OfferingType type) {
        givenRequest(type, RequestStatus.PENDIENTE);
    }

    private void givenRequest(OfferingType type, RequestStatus status) {
        givenRequest(type, status, FUTURE_START);
    }

    private void givenRequest(OfferingType type, RequestStatus status, LocalDateTime slotStart) {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(PROVIDER_ID, Role.PROVIDER)));
        when(serviceRequestRepository.findDetailsById(REQUEST_ID)).thenReturn(Optional.of(details(type, status, slotStart)));
    }

    private AcceptServiceRequestRequest accept() {
        return new AcceptServiceRequestRequest(PROVIDER_ID);
    }

    private RejectServiceRequestRequest reject() {
        return new RejectServiceRequestRequest(PROVIDER_ID, REASON);
    }

    private ServiceRequestHistory savedHistory() {
        ArgumentCaptor<ServiceRequestHistory> history = ArgumentCaptor.forClass(ServiceRequestHistory.class);
        verify(historyRepository).saveHistory(history.capture());
        return history.getValue();
    }

    private void verifyNothingChanged() {
        verify(serviceRequestRepository, never()).updateIfPending(any());
        verify(timeSlotRepository, never()).release(any());
        verify(historyRepository, never()).saveHistory(any());
    }

    @Test
    void acceptingStoresTheAnswerKeepsTheSlotAndWritesHistory() {
        givenPendingRequestOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        ServiceRequestResponse response = service.acceptServiceRequest(REQUEST_ID, accept());

        ArgumentCaptor<ServiceRequest> stored = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).updateIfPending(stored.capture());
        assertThat(stored.getValue().getStatus()).isEqualTo(RequestStatus.ACEPTADO);
        verify(timeSlotRepository, never()).release(any());

        ServiceRequestHistory history = savedHistory();
        assertThat(history.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(history.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(history.getStatus()).isEqualTo(RequestStatus.ACEPTADO);
        assertThat(history.getReason()).isNull();
        assertThat(history.getChangedAt()).isEqualTo(NOW);

        assertThat(response.id()).isEqualTo(REQUEST_ID);
        assertThat(response.status()).isEqualTo(RequestStatus.ACEPTADO);
        assertThat(response.petName()).isEqualTo("Firulais");
        assertThat(response.offeringName()).isEqualTo("Baño completo");
        assertThat(response.providerName()).isEqualTo("Vet Sol");
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void rejectingServiceStoresTheReasonReleasesTheSlotAndWritesHistory() {
        givenPendingRequestOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        ServiceRequestResponse response = service.rejectServiceRequest(REQUEST_ID, reject());

        ArgumentCaptor<ServiceRequest> stored = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).updateIfPending(stored.capture());
        assertThat(stored.getValue().getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(stored.getValue().getRejectionReason()).isEqualTo(REASON);
        verify(timeSlotRepository).release(SLOT_ID);

        ServiceRequestHistory history = savedHistory();
        assertThat(history.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(history.getReason()).isEqualTo(REASON);

        assertThat(response.status()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(response.rejectionReason()).isEqualTo(REASON);
    }

    @Test
    void storesTheAnswerThenReleasesTheSlotThenWritesHistory() {
        givenPendingRequestOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        service.rejectServiceRequest(REQUEST_ID, reject());

        InOrder order = inOrder(serviceRequestRepository, timeSlotRepository, historyRepository);
        order.verify(serviceRequestRepository).updateIfPending(any());
        order.verify(timeSlotRepository).release(SLOT_ID);
        order.verify(historyRepository).saveHistory(any());
    }

    @Test
    void cannotAcceptOnceTheSlotStartTimeIsReached() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.PENDIENTE, NOW);

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(TimeSlotUnavailableException.class);
        verifyNothingChanged();
    }

    @Test
    void canAcceptJustBeforeTheSlotStarts() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.PENDIENTE, NOW.plusMinutes(1));
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        ServiceRequestResponse response = service.acceptServiceRequest(REQUEST_ID, accept());

        assertThat(response.status()).isEqualTo(RequestStatus.ACEPTADO);
    }

    @Test
    void cannotAcceptAProductOncePickupTimeIsReached() {
        givenRequest(OfferingType.PRODUCTO, RequestStatus.PENDIENTE, NOW);

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(TimeSlotUnavailableException.class);
        verifyNothingChanged();
    }

    @Test
    void answeredRequestReportsThatEvenIfTheSlotAlreadyStarted() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.RECHAZADO, NOW.minusDays(1));

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(RequestNotPendingException.class);
        verifyNothingChanged();
    }

    @Test
    void rejectingAfterTheSlotStartTimeIsReachedKeepsTheSlotOccupied() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.PENDIENTE, NOW);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        ServiceRequestResponse response = service.rejectServiceRequest(REQUEST_ID, reject());

        verify(timeSlotRepository, never()).release(any());
        assertThat(savedHistory().getReason()).isEqualTo(REASON);
        assertThat(response.status()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(response.rejectionReason()).isEqualTo(REASON);
    }

    @Test
    void rejectingJustBeforeTheSlotStartsReleasesIt() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.PENDIENTE, NOW.plusMinutes(1));
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        service.rejectServiceRequest(REQUEST_ID, reject());

        verify(timeSlotRepository).release(SLOT_ID);
    }

    @Test
    void rejectingProductDoesNotTouchItsSlot() {
        givenPendingRequestOf(OfferingType.PRODUCTO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(true);

        service.rejectServiceRequest(REQUEST_ID, reject());

        verify(timeSlotRepository, never()).release(any());
        assertThat(savedHistory().getStatus()).isEqualTo(RequestStatus.RECHAZADO);
    }

    @Test
    void rejectionThatLosesTheRaceNeitherReleasesTheSlotNorWritesHistory() {
        givenPendingRequestOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(false);

        assertThatThrownBy(() -> service.rejectServiceRequest(REQUEST_ID, reject()))
                .isInstanceOf(RequestNotPendingException.class);
        verify(timeSlotRepository, never()).release(any());
        verify(historyRepository, never()).saveHistory(any());
    }

    @Test
    void acceptanceThatLosesTheRaceDoesNotWriteHistory() {
        givenPendingRequestOf(OfferingType.SERVICIO);
        when(serviceRequestRepository.updateIfPending(any())).thenReturn(false);

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(RequestNotPendingException.class);
        verify(historyRepository, never()).saveHistory(any());
    }

    @Test
    void cannotAnswerARequestThatIsNotPending() {
        givenRequest(OfferingType.SERVICIO, RequestStatus.ACEPTADO);

        assertThatThrownBy(() -> service.rejectServiceRequest(REQUEST_ID, reject()))
                .isInstanceOf(RequestNotPendingException.class);
        verifyNothingChanged();
    }

    @Test
    void blankReasonChangesNothing() {
        givenPendingRequestOf(OfferingType.SERVICIO);

        assertThatThrownBy(() -> service.rejectServiceRequest(REQUEST_ID, new RejectServiceRequestRequest(PROVIDER_ID, "  ")))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNothingChanged();
    }

    @Test
    void cannotAnswerARequestOfAnotherProvider() {
        UUID otherProviderId = UUID.randomUUID();
        when(providerRepository.findById(otherProviderId)).thenReturn(Optional.of(provider(otherProviderId, Role.PROVIDER)));
        when(serviceRequestRepository.findDetailsById(REQUEST_ID))
                .thenReturn(Optional.of(details(OfferingType.SERVICIO, RequestStatus.PENDIENTE)));

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, new AcceptServiceRequestRequest(otherProviderId)))
                .isInstanceOf(NotRequestProviderException.class);
        verifyNothingChanged();
    }

    @Test
    void rejectsUnknownRequest() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(PROVIDER_ID, Role.PROVIDER)));
        when(serviceRequestRepository.findDetailsById(REQUEST_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(ServiceRequestNotFoundException.class);
        verifyNothingChanged();
    }

    @Test
    void rejectsAccountWithoutProviderRole() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.of(provider(PROVIDER_ID, Role.USER)));

        assertThatThrownBy(() -> service.acceptServiceRequest(REQUEST_ID, accept()))
                .isInstanceOf(RoleNotAllowedException.class);
        verify(serviceRequestRepository, never()).findDetailsById(any());
        verifyNothingChanged();
    }

    @Test
    void rejectsUnknownProvider() {
        when(providerRepository.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rejectServiceRequest(REQUEST_ID, reject()))
                .isInstanceOf(ProviderNotFoundException.class);
        verifyNothingChanged();
    }
}
