package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.TypePet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetJpa {

    @Id
    private UUID id;

    // Owning side of the relationship: holds the owner_id foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerJpa owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_pet", nullable = false, length = 20)
    private TypePet typePet;

    @Column(nullable = false)
    private int age;

    @Column(length = 500)
    private String description;
}
