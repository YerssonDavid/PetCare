package com.petcare.petcarebackend.infrastructure.entity;

import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderJpa {

    @Id
    private UUID id;

    @Column(name = "name_commercial", nullable = false, length = 100, unique = true)
    private String nameCommercial;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_provider", nullable = false, length = 20)
    private TypeProvider typeProvider;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 16)
    private String phone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "experience_years", nullable = false)
    private int experienceYears;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
}
