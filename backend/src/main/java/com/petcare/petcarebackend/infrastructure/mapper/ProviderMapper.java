package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;

public final class ProviderMapper {

    private ProviderMapper() {
    }

    public static ProviderJpa toJpa(Provider provider) {
        return new ProviderJpa(
                provider.getId(),
                provider.getNameCommercial().value(),
                provider.getTypeProvider(),
                provider.getPasswordHash(),
                provider.getDescription(),
                provider.getPhone().value(),
                provider.getEmail().value(),
                provider.getCity(),
                provider.getExperienceYears(),
                provider.getAddress(),
                provider.getRole()
        );
    }

    public static Provider toDomain(ProviderJpa entity) {
        return new Provider(
                entity.getId(),
                new CommercialName(entity.getNameCommercial()),
                entity.getTypeProvider(),
                entity.getPasswordHash(),
                entity.getDescription(),
                new Phone(entity.getPhone()),
                new Email(entity.getEmail()),
                entity.getCity(),
                entity.getExperienceYears(),
                entity.getAddress(),
                entity.getRole()
        );
    }
}
