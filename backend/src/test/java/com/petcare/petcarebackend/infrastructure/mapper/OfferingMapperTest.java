package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Offering;
import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.valueobject.OfferingName;
import com.petcare.petcarebackend.domain.valueobject.Price;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OfferingMapperTest {

    // Every field gets a distinct value so a swapped mapping is caught
    private Offering offering(UUID providerId) {
        return new Offering(UUID.randomUUID(), providerId, new OfferingName("Name"), "description", "conditions",
                new Price(new BigDecimal("45000")), OfferingCategory.PELUQUERIA, OfferingType.PRODUCTO, "image-url");
    }

    private ProviderJpa provider(UUID id) {
        ProviderJpa provider = new ProviderJpa();
        provider.setId(id);
        return provider;
    }

    @Test
    void toJpaCopiesEveryField() {
        UUID providerId = UUID.randomUUID();
        ProviderJpa provider = provider(providerId);
        Offering offering = offering(providerId);

        OfferingJpa entity = OfferingMapper.toJpa(offering, provider);

        assertThat(entity.getId()).isEqualTo(offering.getId());
        assertThat(entity.getProvider()).isSameAs(provider);
        assertThat(entity.getName()).isEqualTo("Name");
        assertThat(entity.getNameNormalized()).isEqualTo("name");
        assertThat(entity.getDescription()).isEqualTo("description");
        assertThat(entity.getConditions()).isEqualTo("conditions");
        assertThat(entity.getPrice()).isEqualByComparingTo("45000");
        assertThat(entity.getCategory()).isEqualTo(OfferingCategory.PELUQUERIA);
        assertThat(entity.getType()).isEqualTo(OfferingType.PRODUCTO);
        assertThat(entity.getImageUrl()).isEqualTo("image-url");
    }

    @Test
    void toDomainRebuildsTheSameOffering() {
        UUID providerId = UUID.randomUUID();
        Offering original = offering(providerId);

        Offering restored = OfferingMapper.toDomain(OfferingMapper.toJpa(original, provider(providerId)));

        // Price is a record, so recursive comparison checks its value
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
