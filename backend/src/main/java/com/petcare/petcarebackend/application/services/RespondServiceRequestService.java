package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.AcceptServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.RejectServiceRequestRequest;
import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;
import com.petcare.petcarebackend.application.exception.NotRequestProviderException;
import com.petcare.petcarebackend.application.exception.NotRequestParticipantException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.exception.RoleNotAllowedException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.exception.TimeSlotUnavailableException;
import com.petcare.petcarebackend.application.port.in.registry.ServiceRequestAnswerRegistry;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.entity.ServiceRequest;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.RequestStatus;
import com.petcare.petcarebackend.domain.exception.RequestNotPendingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RespondServiceRequestService implements ServiceRequestAnswerRegistry {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final ServiceRequestHistoryRepositoryPort historyRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final ProviderRepositoryPort providerRepository;
    private final Clock clock;

    @Override
    @Transactional
    public ServiceRequestResponse acceptServiceRequest(UUID requestId, AcceptServiceRequestRequest request) {
        ServiceRequestDetails details = loadForProvider(requestId, request.providerId());
        LocalDateTime now = LocalDateTime.now(clock);

        details.request().accept();
        if (details.slot().hasStarted(now)) {
            throw new TimeSlotUnavailableException("The time slot has already started");
        }
        storeAnswer(details.request());
        saveHistory(details.request(), now);

        return ServiceRequestResponse.from(details);
    }

    @Override
    @Transactional
    public ServiceRequestResponse rejectServiceRequest(UUID requestId, RejectServiceRequestRequest request) {
        ServiceRequestDetails details = loadForProvider(requestId, request.providerId());
        LocalDateTime now = LocalDateTime.now(clock);

        details.request().reject(request.reason());
        storeAnswer(details.request());

        // Runs after storeAnswer so an answer that lost the race never frees a slot;
        // the slot of a product was never occupied, and a slot that already started stays occupied
        if (details.offering().getType() == OfferingType.SERVICIO && !details.slot().hasStarted(now)) {
            timeSlotRepository.release(details.slot().getId());
        }
        saveHistory(details.request(), now);

        return ServiceRequestResponse.from(details);
    }

    @Override
    @Transactional
    public ServiceRequestResponse cancelServiceRequest(UUID requestId, UUID ownerId) {
        ServiceRequestDetails details = serviceRequestRepository.findDetailsById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
        if (!details.request().getOwnerId().equals(ownerId)) {
            throw new NotRequestParticipantException();
        }
        details.request().setStatus(RequestStatus.CANCELADA);
        if (!serviceRequestRepository.updateIfPending(details.request())) {
            throw new RequestNotPendingException();
        }
        timeSlotRepository.release(details.slot().getId());
        LocalDateTime now = LocalDateTime.now(clock);
        historyRepository.saveHistory(ServiceRequestHistory.of(details.request(), now));
        return ServiceRequestResponse.from(details);
    }

    private ServiceRequestDetails loadForProvider(UUID requestId, UUID providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (provider.getRole() != Role.PROVIDER) {
            throw new RoleNotAllowedException("Only providers can answer requests");
        }

        ServiceRequestDetails details = serviceRequestRepository.findDetailsById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));

        if (!details.request().getProviderId().equals(provider.getId())) {
            throw new NotRequestProviderException();
        }
        return details;
    }

    private void storeAnswer(ServiceRequest request) {
        if (!serviceRequestRepository.updateIfPending(request)) {
            throw new RequestNotPendingException();
        }
    }

    // Must run after the conditional updates: they clear the persistence context,
    // which would discard a history row that was not flushed yet
    private void saveHistory(ServiceRequest request, LocalDateTime now) {
        historyRepository.saveHistory(ServiceRequestHistory.of(request, now));
    }
}
