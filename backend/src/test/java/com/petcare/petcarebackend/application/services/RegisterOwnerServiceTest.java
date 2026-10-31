package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterOwnerRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.OwnerAlreadyExistsException;
import com.petcare.petcarebackend.application.port.out.OwnerRepositoryPort;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.domain.entity.Owner;
import com.petcare.petcarebackend.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterOwnerServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OwnerRepositoryPort ownerRepository;

    @InjectMocks
    private RegisterOwnerService service;

    private RegisterOwnerRequest request(String email) {
        return new RegisterOwnerRequest("Ana", email, "311 111 1111", "secret123");
    }

    @Test
    void savesOwnerWithHashedPasswordAndReturnsItsId() {
        when(ownerRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        RegisterResponse response = service.registerOwner(request("Ana@Mail.com"));

        ArgumentCaptor<Owner> saved = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository).saveOwner(saved.capture());
        Owner owner = saved.getValue();
        assertThat(owner.getEmail().value()).isEqualTo("ana@mail.com");
        assertThat(owner.getPhone().value()).isEqualTo("3111111111");
        assertThat(owner.getPasswordHash()).isEqualTo("hashed");
        assertThat(owner.getRole()).isEqualTo(Role.USER);
        assertThat(owner.getActiveAccount()).isTrue();
        assertThat(response.id()).isNotNull().isEqualTo(owner.getId());
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        when(ownerRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerOwner(request("ANA@mail.com")))
                .isInstanceOf(OwnerAlreadyExistsException.class);
        verify(passwordEncoder, never()).encode(anyString());
        verify(ownerRepository, never()).saveOwner(any());
    }

    @Test
    void rejectsInvalidEmailBeforeTouchingRepository() {
        assertThatThrownBy(() -> service.registerOwner(request("not-an-email")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(ownerRepository, never()).existsByEmail(anyString());
        verify(ownerRepository, never()).saveOwner(any());
    }
}
