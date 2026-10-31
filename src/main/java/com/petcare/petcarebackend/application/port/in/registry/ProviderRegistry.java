package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.RegisterProviderRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

public interface ProviderRegistry {
    RegisterResponse registerProvider(RegisterProviderRequest request);
}
