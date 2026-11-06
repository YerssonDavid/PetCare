package com.petcare.petcarebackend.infrastructure.adapter.persistence;

import com.petcare.petcarebackend.application.port.out.request.ServiceRequestHistoryRepositoryPort;
import com.petcare.petcarebackend.domain.entity.ServiceRequestHistory;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ServiceRequestHistoryRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ServiceRequestRepository;
import com.petcare.petcarebackend.infrastructure.mapper.ServiceRequestHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceRequestHistoryJpaAdapter implements ServiceRequestHistoryRepositoryPort {

    private final ServiceRequestHistoryRepository historyRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public void saveHistory(ServiceRequestHistory history) {
        // Proxies without SELECT: the use case already saved the request and verified the owner
        historyRepository.save(ServiceRequestHistoryMapper.toJpa(
                history,
                serviceRequestRepository.getReferenceById(history.getRequestId()),
                ownerRepository.getReferenceById(history.getOwnerId())
        ));
    }

    @Override
    public List<ServiceRequestHistory> findByRequestId(UUID requestId) {
        return historyRepository.findByRequestIdOrderByChangedAtDesc(requestId).stream()
                .map(ServiceRequestHistoryMapper::toDomain)
                .toList();
    }
}
