package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.PublishOfferingRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

public interface OfferingRegistry {
    RegisterResponse publishOffering(PublishOfferingRequest request);
}
