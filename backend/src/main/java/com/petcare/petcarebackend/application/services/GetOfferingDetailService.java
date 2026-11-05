package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.OfferingDetailResponse;
import com.petcare.petcarebackend.application.exception.OfferingNotFoundException;
import com.petcare.petcarebackend.application.exception.ProviderNotFoundException;
import com.petcare.petcarebackend.application.port.in.query.OfferingDetailQuery;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.application.port.out.provider.ProviderRepositoryPort;
import com.petcare.petcarebackend.application.port.out.timeslot.TimeSlotRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.entity.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOfferingDetailService implements OfferingDetailQuery {

    private final OfferingRepositoryPort offeringRepository;
    private final ProviderRepositoryPort providerRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final Clock clock;

    @Override
    public OfferingDetailResponse getOfferingDetail(UUID offeringId) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new OfferingNotFoundException(offeringId));

        Provider provider = providerRepository.findById(offering.getProviderId())
                .orElseThrow(() -> new ProviderNotFoundException(offering.getProviderId()));

        return OfferingDetailResponse.from(
                offering,
                provider,
                timeSlotRepository.findFreeByOfferingStartingAfter(offeringId, LocalDateTime.now(clock))
        );
    }
}
