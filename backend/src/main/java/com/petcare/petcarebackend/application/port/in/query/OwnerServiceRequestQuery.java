package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.ServiceRequestResponse;

import java.util.List;
import java.util.UUID;

public interface OwnerServiceRequestQuery {
    List<ServiceRequestResponse> listOwnerServiceRequests(UUID ownerId);
}
