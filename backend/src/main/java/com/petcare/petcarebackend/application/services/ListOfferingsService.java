package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.application.port.in.query.OfferingQuery;
import com.petcare.petcarebackend.application.port.out.offering.OfferingRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListOfferingsService implements OfferingQuery {

    private final OfferingRepositoryPort offeringRepository;

    @Override
    public List<OfferingResponse> listOfferings(OfferingCategory category) {
        List<Offering> offerings = category == null
                ? offeringRepository.findAll()
                : offeringRepository.findByCategory(category);

        return offerings.stream()
                .map(OfferingResponse::from)
                .toList();
    }
}
