package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

import java.util.UUID;

public interface OfferingRegistry {
    RegisterResponse publishOffering(UUID providerId, PublishOfferingRequest request);
}
