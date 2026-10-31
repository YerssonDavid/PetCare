package com.petcare.petcarebackend.application.port.in.registry;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;

public interface OwnerRegistry {
    RegisterResponse registerOwner(RegisterOwnerRequest request);
}
