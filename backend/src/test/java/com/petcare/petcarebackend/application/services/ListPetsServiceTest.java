package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.PetResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.port.out.owner.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.pet.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.enums.TypePet;
import com.petcare.petcarebackend.domain.valueobject.PetName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPetsServiceTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    @Mock
    private PetRepositoryPort petRepository;

    @Mock
    private OwnerRepositoryPort ownerRepository;

    @InjectMocks
    private ListPetsService service;

    @Test
    void returnsThePetsOfTheOwner() {
        Pet pet = Pet.register(OWNER_ID, new PetName("Luna"), firstType(), 3, "Tranquila");
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(true);
        when(petRepository.findPetsByIdOwner(OWNER_ID)).thenReturn(List.of(pet));

        List<PetResponse> pets = service.listPetsByOwner(OWNER_ID);

        assertThat(pets).containsExactly(
                new PetResponse(pet.getId(), "Luna", firstType(), 3, "Tranquila"));
    }

    @Test
    void returnsEmptyListWhenOwnerHasNoPets() {
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(true);
        when(petRepository.findPetsByIdOwner(OWNER_ID)).thenReturn(List.of());

        assertThat(service.listPetsByOwner(OWNER_ID)).isEmpty();
    }

    @Test
    void rejectsUnknownOwner() {
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listPetsByOwner(OWNER_ID))
                .isInstanceOf(OwnerNotFoundException.class);
        verify(petRepository, never()).findPetsByIdOwner(any());
    }

    private TypePet firstType() {
        return TypePet.values()[0];
    }
}
