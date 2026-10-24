package com.petcare.petcarebackend.application.port.out;

import com.petcare.petcarebackend.application.dto.ResponseHttp;
import com.petcare.petcarebackend.domain.entity.Owner;

public interface OwnerRepositoryPort {
    ResponseHttp saveOwner(Owner owner);
    boolean existsByEmail(String email);
}
