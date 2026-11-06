package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;

import java.util.List;
import java.util.UUID;

public interface ProvidedServiceRequestQuery {
    // Provided means a service, not a product, that was accepted and whose slot start time has been reached
    List<ReceivedServiceRequestResponse> listProvidedServiceRequests(UUID providerId);
}
