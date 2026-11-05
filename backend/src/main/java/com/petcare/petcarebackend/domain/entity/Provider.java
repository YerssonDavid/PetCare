package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import com.petcare.petcarebackend.domain.valueobject.CommercialName;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;

import java.util.UUID;

public class Provider {
    private UUID id;
    private CommercialName nameCommercial;
    private TypeProvider typeProvider;
    private String passwordHash;
    private String description;
    private Phone phone;
    private Email email;
    private String city;
    private int experienceYears;
    private String address;
    private Role role;

    public Provider() {
    }

    public Provider(UUID id, CommercialName nameCommercial, TypeProvider typeProvider, String passwordHash,
                    String description, Phone phone, Email email, String city,
                    int experienceYears, String address, Role role) {
        this.id = id;
        this.nameCommercial = nameCommercial;
        this.typeProvider = typeProvider;
        this.passwordHash = passwordHash;
        this.description = description;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.experienceYears = experienceYears;
        this.address = address;
        this.role = role;
    }

    // Create factory
    public static Provider register(CommercialName nameCommercial, TypeProvider typeProvider, String passwordHash,
                                    String description, Phone phone, Email email, String city,
                                    int experienceYears, String address){
        return new Provider(UUID.randomUUID(), nameCommercial, typeProvider, passwordHash, description,
                phone, email, city, experienceYears, address, Role.PROVIDER);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CommercialName getNameCommercial() {
        return nameCommercial;
    }

    public void setNameCommercial(CommercialName nameCommercial) {
        this.nameCommercial = nameCommercial;
    }

    public TypeProvider getTypeProvider() {
        return typeProvider;
    }

    public void setTypeProvider(TypeProvider typeProvider) {
        this.typeProvider = typeProvider;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
