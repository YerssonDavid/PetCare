package com.petcare.petcarebackend.infrastructure.mapper;

import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.PetJpa;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PetMapperTest {

    private Pet pet(UUID ownerId) {
        return new Pet(UUID.randomUUID(), ownerId, new PetName("Luna"), TypePet.PERRO, 3, "description");
    }

    private OwnerJpa owner(UUID id) {
        OwnerJpa owner = new OwnerJpa();
        owner.setId(id);
        return owner;
    }

    @Test
    void toJpaCopiesEveryField() {
        UUID ownerId = UUID.randomUUID();
        OwnerJpa owner = owner(ownerId);
        Pet pet = pet(ownerId);

        PetJpa entity = PetMapper.toJpa(pet, owner);

        assertThat(entity.getId()).isEqualTo(pet.getId());
        assertThat(entity.getOwner()).isSameAs(owner);
        assertThat(entity.getName()).isEqualTo("Luna");
        assertThat(entity.getNameNormalized()).isEqualTo("luna");
        assertThat(entity.getTypePet()).isEqualTo(TypePet.PERRO);
        assertThat(entity.getAge()).isEqualTo(3);
        assertThat(entity.getDescription()).isEqualTo("description");
    }

    @Test
    void toDomainRebuildsTheSamePet() {
        UUID ownerId = UUID.randomUUID();
        Pet original = pet(ownerId);

        Pet restored = PetMapper.toDomain(PetMapper.toJpa(original, owner(ownerId)));

        // PetName is a record, so recursive comparison checks its value
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
