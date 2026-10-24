package com.petcare.petcarebackend.application.port.in;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.ResponseHttp;

public interface OwnerRegistry {
    ResponseHttp registerOwner(RegisterOwnerRequest request);
}
