package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderMapperTest {

    // Every field gets a distinct value so a swapped mapping is caught
    private Provider provider() {
        return new Provider(UUID.randomUUID(), new CommercialName("Vet Sol"), TypeProvider.INDEPENDIENTE, "hashed",
                "description", new Phone("3111111111"), new Email("vet@sol.com"), "Bogotá", 7, "Calle 1", Role.PROVIDER);
    }

    @Test
    void toJpaCopiesEveryField() {
        Provider provider = provider();

        ProviderJpa entity = ProviderMapper.toJpa(provider);

        assertThat(entity.getId()).isEqualTo(provider.getId());
        assertThat(entity.getNameCommercial()).isEqualTo("Vet Sol");
        assertThat(entity.getTypeProvider()).isEqualTo(TypeProvider.INDEPENDIENTE);
        assertThat(entity.getPasswordHash()).isEqualTo("hashed");
        assertThat(entity.getDescription()).isEqualTo("description");
        assertThat(entity.getPhone()).isEqualTo("3111111111");
        assertThat(entity.getEmail()).isEqualTo("vet@sol.com");
        assertThat(entity.getCity()).isEqualTo("Bogotá");
        assertThat(entity.getExperienceYears()).isEqualTo(7);
        assertThat(entity.getAddress()).isEqualTo("Calle 1");
        assertThat(entity.getRole()).isEqualTo(Role.PROVIDER);
    }

    @Test
    void toDomainRebuildsTheSameProvider() {
        Provider original = provider();

        Provider restored = ProviderMapper.toDomain(ProviderMapper.toJpa(original));

        // Value objects are records, so recursive comparison checks their values
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
