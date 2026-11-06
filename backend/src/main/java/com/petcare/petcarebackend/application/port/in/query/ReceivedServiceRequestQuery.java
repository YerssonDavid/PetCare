package com.petcare.petcarebackend.application.port.in.query;

import com.petcare.petcarebackend.application.dto.ReceivedServiceRequestResponse;
import com.petcare.petcarebackend.domain.enums.RequestStatus;

import java.util.List;
import java.util.UUID;

public interface ReceivedServiceRequestQuery {
    // A null status returns the requests in every status
    List<ReceivedServiceRequestResponse> listReceivedServiceRequests(UUID providerId, RequestStatus status);
}
