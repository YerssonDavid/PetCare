package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.OfferingResponse;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;

import java.util.List;

public interface OfferingQuery {
    List<OfferingResponse> listOfferings(OfferingCategory category);
}
