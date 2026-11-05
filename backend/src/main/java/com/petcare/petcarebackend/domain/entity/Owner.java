package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.valueobject.Email;
import com.petcare.petcarebackend.domain.valueobject.Phone;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Owner {
    private UUID id;
    private String name;
    private Email email;
    private Phone phone;
    private String passwordHash;
    private Role role;
    private Boolean activeAccount;
    private LocalDateTime createdAt;
    private List<Pet> pets = new ArrayList<>();

    public Owner() {
    }

    public Owner(UUID id, String name, Email email, Phone phone, String passwordHash,
                 Role role, Boolean activeAccount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.activeAccount = activeAccount;
        this.createdAt = createdAt;
    }

    // Create factory
    public static Owner register(String name, Email email, Phone phone, String passwordHash){
        return new Owner(UUID.randomUUID(), name, email, phone, passwordHash, Role.USER, true, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getActiveAccount() {
        return activeAccount;
    }

    public void setActiveAccount(Boolean activeAccount) {
        this.activeAccount = activeAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}
