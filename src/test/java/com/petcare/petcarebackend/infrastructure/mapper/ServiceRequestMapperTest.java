package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.domain.enums.PurchaseType;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.valueobject.Price;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestHistoryJpa;
import com.petcare.petcarebackend.infrastructure.entity.ServiceRequestJpa;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRequestMapperTest {

    private static final LocalDateTime AT = LocalDateTime.of(2026, 11, 10, 8, 0);

    @Test
    void requestToJpaCopiesEveryField() {
        ServiceRequest request = new ServiceRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new Price(new BigDecimal("45000")),
                RequestStatus.RECHAZADO, PurchaseType.ENTREGA, "reason", AT);
        OwnerJpa owner = new OwnerJpa();
        PetJpa pet = new PetJpa();
        OfferingJpa offering = new OfferingJpa();
        ProviderJpa provider = new ProviderJpa();
        TimeSlotJpa slot = new TimeSlotJpa();

        ServiceRequestJpa entity = ServiceRequestMapper.toJpa(request, owner, pet, offering, provider, slot);

        assertThat(entity.getId()).isEqualTo(request.getId());
        assertThat(entity.getOwner()).isSameAs(owner);
        assertThat(entity.getPet()).isSameAs(pet);
        assertThat(entity.getOffering()).isSameAs(offering);
        assertThat(entity.getProvider()).isSameAs(provider);
        assertThat(entity.getSlot()).isSameAs(slot);
        assertThat(entity.getPrice()).isEqualByComparingTo("45000");
        assertThat(entity.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(entity.getPurchaseType()).isEqualTo(PurchaseType.ENTREGA);
        assertThat(entity.getRejectionReason()).isEqualTo("reason");
        assertThat(entity.getRequestedAt()).isEqualTo(AT);
    }

    @Test
    void requestToDomainCopiesEveryField() {
        OwnerJpa owner = new OwnerJpa();
        owner.setId(UUID.randomUUID());
        PetJpa pet = new PetJpa();
        pet.setId(UUID.randomUUID());
        OfferingJpa offering = new OfferingJpa();
        offering.setId(UUID.randomUUID());
        ProviderJpa provider = new ProviderJpa();
        provider.setId(UUID.randomUUID());
        TimeSlotJpa slot = new TimeSlotJpa();
        slot.setId(UUID.randomUUID());
        ServiceRequestJpa entity = new ServiceRequestJpa(UUID.randomUUID(), owner, pet, offering, provider, slot,
                new BigDecimal("45000"), RequestStatus.RECHAZADO, PurchaseType.ENTREGA, "reason", AT);

        ServiceRequest request = ServiceRequestMapper.toDomain(entity);

        assertThat(request.getId()).isEqualTo(entity.getId());
        assertThat(request.getOwnerId()).isEqualTo(owner.getId());
        assertThat(request.getPetId()).isEqualTo(pet.getId());
        assertThat(request.getOfferingId()).isEqualTo(offering.getId());
        assertThat(request.getProviderId()).isEqualTo(provider.getId());
        assertThat(request.getSlotId()).isEqualTo(slot.getId());
        assertThat(request.getPrice().value()).isEqualByComparingTo("45000");
        assertThat(request.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(request.getPurchaseType()).isEqualTo(PurchaseType.ENTREGA);
        assertThat(request.getRejectionReason()).isEqualTo("reason");
        assertThat(request.getRequestedAt()).isEqualTo(AT);
    }

    @Test
    void historyToJpaCopiesEveryField() {
        ServiceRequestHistory history = new ServiceRequestHistory(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), RequestStatus.RECHAZADO, "reason", AT);
        ServiceRequestJpa request = new ServiceRequestJpa();
        OwnerJpa owner = new OwnerJpa();

        ServiceRequestHistoryJpa entity = ServiceRequestHistoryMapper.toJpa(history, request, owner);

        assertThat(entity.getId()).isEqualTo(history.getId());
        assertThat(entity.getRequest()).isSameAs(request);
        assertThat(entity.getOwner()).isSameAs(owner);
        assertThat(entity.getStatus()).isEqualTo(RequestStatus.RECHAZADO);
        assertThat(entity.getReason()).isEqualTo("reason");
        assertThat(entity.getChangedAt()).isEqualTo(AT);
    }
}
