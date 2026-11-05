package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.TypePet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pets", uniqueConstraints = @UniqueConstraint(
        name = PetJpa.UK_OWNER_NAME,
        columnNames = {"owner_id", "name_normalized"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetJpa {

    public static final String UK_OWNER_NAME = "uk_pets_owner_name_normalized";

    @Id
    private UUID id;

    // Owning side of the relationship: holds the owner_id foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerJpa owner;

    @Column(nullable = false, length = 100)
    private String name;

    // Lowercased copy of name; only used to enforce one name per owner at DB level
    @Column(name = "name_normalized", nullable = false, length = 100)
    private String nameNormalized;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_pet", nullable = false, length = 20)
    private TypePet typePet;

    @Column(nullable = false)
    private int age;

    @Column(length = 500)
    private String description;
}
