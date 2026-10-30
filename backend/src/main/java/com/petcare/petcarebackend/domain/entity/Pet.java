package com.petcare.petcarebackend.domain.entity;

import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.domain.valueobject.PetName;

import java.util.UUID;

public class Pet {
    private UUID id;
    private UUID ownerId;
    private PetName name;
    private TypePet typePet;
    private int age;
    private String description;

    public Pet() {
    }

    public Pet(UUID id, UUID ownerId, PetName name, TypePet typePet, int age, String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.typePet = typePet;
        this.age = age;
        this.description = description;
    }

    // Create factory
    public static Pet register(UUID ownerId, PetName name, TypePet typePet, int age, String description){
        return new Pet(UUID.randomUUID(), ownerId, name, typePet, age, description);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public PetName getName() {
        return name;
    }

    public void setName(PetName name) {
        this.name = name;
    }

    public TypePet getTypePet() {
        return typePet;
    }

    public void setTypePet(TypePet typePet) {
        this.typePet = typePet;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
