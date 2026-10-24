package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TypeProvider;

import java.util.UUID;

public class Provider {
    private UUID id;
    private String nameCommercial;
    private TypeProvider typeProvider;
    private String passwordHash;
    private String description;
    private String phone;
    private String email;
    private String city;
    private int experienceYears;
    private String address;
    private Role role;
}
