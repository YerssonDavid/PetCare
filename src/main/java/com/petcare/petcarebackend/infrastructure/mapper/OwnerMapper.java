package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;

public final class OwnerMapper {

    private OwnerMapper() {
    }

    public static Owner toDomain(OwnerJpa entity) {
        return new Owner(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                new Phone(entity.getPhone()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getActiveAccount(),
                entity.getCreatedAt()
        );
    }
}
