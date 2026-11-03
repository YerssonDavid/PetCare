package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.PurchaseType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceRequestTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID PET_ID = UUID.randomUUID();
    private static final UUID PROVIDER_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 8, 0);

    private Offering offering(OfferingType type) {
        return new Offering(UUID.randomUUID(), PROVIDER_ID, new OfferingName("Baño"), "d", "c",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, type, null);
    }

    private TimeSlot slot(Offering offering) {
        return new TimeSlot(UUID.randomUUID(), offering.getId(), LocalDate.of(2026, 11, 11),
                LocalTime.of(10, 0), LocalTime.of(11, 0), SlotStatus.LIBRE);
    }

    @Test
    void createsPendingRequestLinkedToEveryParty() {
        Offering offering = offering(OfferingType.SERVICIO);
        TimeSlot slot = slot(offering);

        ServiceRequest request = ServiceRequest.create(OWNER_ID, PET_ID, offering, slot, null, NOW);

        assertThat(request.getId()).isNotNull();
        assertThat(request.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(request.getPetId()).isEqualTo(PET_ID);
        assertThat(request.getOfferingId()).isEqualTo(offering.getId());
        assertThat(request.getProviderId()).isEqualTo(PROVIDER_ID);
        assertThat(request.getSlotId()).isEqualTo(slot.getId());
        assertThat(request.getPrice()).isEqualTo(offering.getPrice());
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
        assertThat(request.getPurchaseType()).isNull();
        assertThat(request.getRejectionReason()).isNull();
        assertThat(request.getRequestedAt()).isEqualTo(NOW);
    }

    @Test
    void productKeepsItsPurchaseType() {
        Offering offering = offering(OfferingType.PRODUCTO);

        ServiceRequest request = ServiceRequest.create(OWNER_ID, PET_ID, offering, slot(offering), PurchaseType.ENTREGA, NOW);

        assertThat(request.getPurchaseType()).isEqualTo(PurchaseType.ENTREGA);
    }

    @Test
    void productRequiresPurchaseType() {
        Offering offering = offering(OfferingType.PRODUCTO);

        assertThatThrownBy(() -> ServiceRequest.create(OWNER_ID, PET_ID, offering, slot(offering), null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void serviceRejectsPurchaseType() {
        Offering offering = offering(OfferingType.SERVICIO);

        assertThatThrownBy(() -> ServiceRequest.create(OWNER_ID, PET_ID, offering, slot(offering), PurchaseType.RETIRO, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void historyCapturesTheCurrentStateOfTheRequest() {
        Offering offering = offering(OfferingType.SERVICIO);
        ServiceRequest request = ServiceRequest.create(OWNER_ID, PET_ID, offering, slot(offering), null, NOW);

        ServiceRequestHistory history = ServiceRequestHistory.of(request, NOW);

        assertThat(history.getId()).isNotNull();
        assertThat(history.getRequestId()).isEqualTo(request.getId());
        assertThat(history.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(history.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
        assertThat(history.getReason()).isNull();
        assertThat(history.getChangedAt()).isEqualTo(NOW);
    }

    private ServiceRequest pendingRequest() {
        Offering offering = offering(OfferingType.SERVICIO);
        return ServiceRequest.create(OWNER_ID, PET_ID, offering, slot(offering), null, NOW);
    }

    @Test
    void acceptsPendingRequest() {
        ServiceRequest request = pendingRequest();

        request.accept();

        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACEPTADO);
        assertThat(request.getRejectionReason()).isNull();
    }

    @Test
    void rejectsPendingRequestKeepingTheTrimmedReason() {
        ServiceRequest request = pendingRequest();

        request.reject("  Sin disponibilidad  ");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(request.getRejectionReason()).isEqualTo("Sin disponibilidad");
    }

    @Test
    void historyOfRejectedRequestCarriesTheReason() {
        ServiceRequest request = pendingRequest();
        request.reject("Sin disponibilidad");

        ServiceRequestHistory history = ServiceRequestHistory.of(request, NOW);

        assertThat(history.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(history.getReason()).isEqualTo("Sin disponibilidad");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectingRequiresAReasonAndLeavesTheRequestPending(String reason) {
        ServiceRequest request = pendingRequest();

        assertThatThrownBy(() -> request.reject(reason)).isInstanceOf(IllegalArgumentException.class);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDIENTE);
    }

    @Test
    void answeredRequestCannotBeAnsweredAgain() {
        ServiceRequest accepted = pendingRequest();
        accepted.accept();
        ServiceRequest rejected = pendingRequest();
        rejected.reject("Sin disponibilidad");

        assertThatThrownBy(accepted::accept).isInstanceOf(RequestNotPendingException.class);
        assertThatThrownBy(() -> accepted.reject("r")).isInstanceOf(RequestNotPendingException.class);
        assertThatThrownBy(rejected::accept).isInstanceOf(RequestNotPendingException.class);
        assertThatThrownBy(() -> rejected.reject("r")).isInstanceOf(RequestNotPendingException.class);
        assertThat(rejected.getRejectionReason()).isEqualTo("Sin disponibilidad");
    }
}
