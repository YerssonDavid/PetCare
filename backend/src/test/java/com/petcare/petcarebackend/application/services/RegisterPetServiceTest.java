package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterPetRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OwnerNotFoundException;
import com.petcare.petcarebackend.application.exception.PetAlreadyExistsException;
import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.PetRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Pet;
import com.petcare.petcarebackend.domain.enums.TypePet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterPetServiceTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    @Mock
    private PetRepositoryPort petRepository;

    @Mock
    private OwnerRepositoryPort ownerRepository;

    @InjectMocks
    private RegisterPetService service;

    private RegisterPetRequest request(String name) {
        return new RegisterPetRequest(OWNER_ID, name, TypePet.PERRO, 3, "cafe");
    }

    @Test
    void savesPetAndReturnsItsId() {
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(true);
        when(petRepository.existsByOwnerIdAndNormalizedName(OWNER_ID, "firulais")).thenReturn(false);

        RegisterResponse response = service.registerPet(request(" Firulais "));

        ArgumentCaptor<Pet> saved = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository).savePet(saved.capture());
        assertThat(saved.getValue().getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(saved.getValue().getName().value()).isEqualTo("Firulais");
        assertThat(response.id()).isNotNull().isEqualTo(saved.getValue().getId());
    }

    @Test
    void rejectsUnknownOwner() {
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.registerPet(request("Firulais")))
                .isInstanceOf(OwnerNotFoundException.class);
        verify(petRepository, never()).savePet(any());
    }

    @Test
    void rejectsDuplicateNameForSameOwner() {
        when(ownerRepository.existsById(OWNER_ID)).thenReturn(true);
        when(petRepository.existsByOwnerIdAndNormalizedName(OWNER_ID, "firulais")).thenReturn(true);

        assertThatThrownBy(() -> service.registerPet(request("FIRULAIS")))
                .isInstanceOf(PetAlreadyExistsException.class);
        verify(petRepository, never()).savePet(any());
    }
}
