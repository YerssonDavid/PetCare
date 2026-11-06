package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "services", uniqueConstraints = @UniqueConstraint(
        name = OfferingJpa.UK_PROVIDER_NAME,
        columnNames = {"provider_id", "name_normalized"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferingJpa {

    public static final String UK_PROVIDER_NAME = "uk_services_provider_name_normalized";

    @Id
    private UUID id;

    // Owning side of the relationship: holds the provider_id foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderJpa provider;

    @Column(nullable = false, length = 100)
    private String name;

    // Lowercased copy of name; only used to enforce one name per provider at DB level
    @Column(name = "name_normalized", nullable = false, length = 100)
    private String nameNormalized;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 500)
    private String conditions;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferingType type;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
