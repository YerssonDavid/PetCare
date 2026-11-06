package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.OfferingDetailResponse;

import java.util.UUID;

public interface OfferingDetailQuery {
    OfferingDetailResponse getOfferingDetail(UUID offeringId);
}
