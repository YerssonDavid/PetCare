package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.ServiceRequestDetails;
import com.petcare.petcarebackend.application.dto.ServiceRequestHistoryResponse;
import com.petcare.petcarebackend.application.exception.NotRequestParticipantException;
import com.petcare.petcarebackend.application.exception.ServiceRequestNotFoundException;
import com.petcare.petcarebackend.application.port.in.query.ServiceRequestHistoryQuery;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.application.port.out.request.ServiceRequestRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetServiceRequestHistoryService implements ServiceRequestHistoryQuery {

    private final ServiceRequestRepositoryPort serviceRequestRepository;
    private final ServiceRequestHistoryRepositoryPort historyRepository;

    @Override
    public ServiceRequestHistoryResponse getServiceRequestHistory(UUID requestId, UUID accountId) {
        ServiceRequestDetails details = serviceRequestRepository.findDetailsById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));

        if (!details.request().involves(accountId)) {
            throw new NotRequestParticipantException();
        }

        return ServiceRequestHistoryResponse.from(details, historyRepository.findByRequestId(requestId));
    }
}
