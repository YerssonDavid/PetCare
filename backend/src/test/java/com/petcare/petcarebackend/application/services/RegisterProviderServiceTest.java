package com.petcare.petcarebackend.application.services;

import com.petcare.petcarebackend.application.dto.RegisterProviderRequest;
import com.petcare.petcarebackend.application.dto.RegisterResponse;
import com.petcare.petcarebackend.application.exception.ProviderAlreadyExistsException;
import com.petcare.petcarebackend.application.port.out.PasswordEncoder;
import com.petcare.petcarebackend.application.port.out.ProviderRepositoryPort;
import com.petcare.petcarebackend.domain.entity.Provider;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
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
class RegisterProviderServiceTest {

    @Mock
    private ProviderRepositoryPort providerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterProviderService service;

    private RegisterProviderRequest request(String name, String email) {
        return new RegisterProviderRequest(name, TypeProvider.CLINICA, email, "+57 (311) 111-1111",
                "secret123", "Bogotá", "Calle 1 # 2-3", 5, "Clínica veterinaria");
    }

    @Test
    void savesProviderWithNormalizedDataAndReturnsItsId() {
        when(providerRepository.existsByName("Vet Sol")).thenReturn(false);
        when(providerRepository.existsByEmail("vet@sol.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        RegisterResponse response = service.registerProvider(request("  Vet   Sol ", "Vet@Sol.com"));

        ArgumentCaptor<Provider> saved = ArgumentCaptor.forClass(Provider.class);
        verify(providerRepository).saveProvider(saved.capture());
        Provider provider = saved.getValue();
        assertThat(provider.getNameCommercial().value()).isEqualTo("Vet Sol");
        assertThat(provider.getEmail().value()).isEqualTo("vet@sol.com");
        assertThat(provider.getPhone().value()).isEqualTo("+573111111111");
        assertThat(provider.getTypeProvider()).isEqualTo(TypeProvider.CLINICA);
        assertThat(provider.getCity()).isEqualTo("Bogotá");
        assertThat(provider.getAddress()).isEqualTo("Calle 1 # 2-3");
        assertThat(provider.getExperienceYears()).isEqualTo(5);
        assertThat(provider.getDescription()).isEqualTo("Clínica veterinaria");
        assertThat(provider.getPasswordHash()).isEqualTo("hashed");
        assertThat(provider.getRole()).isEqualTo(Role.PROVIDER);
        assertThat(response.id()).isNotNull().isEqualTo(provider.getId());
    }

    @Test
    void rejectsAlreadyRegisteredName() {
        when(providerRepository.existsByName("Vet Sol")).thenReturn(true);

        assertThatThrownBy(() -> service.registerProvider(request(" Vet Sol ", "vet@sol.com")))
                .isInstanceOf(ProviderAlreadyExistsException.class);
        verify(passwordEncoder, never()).encode(anyString());
        verify(providerRepository, never()).saveProvider(any());
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        when(providerRepository.existsByName("Vet Sol")).thenReturn(false);
        when(providerRepository.existsByEmail("vet@sol.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerProvider(request("Vet Sol", "VET@sol.com")))
                .isInstanceOf(ProviderAlreadyExistsException.class);
        verify(passwordEncoder, never()).encode(anyString());
        verify(providerRepository, never()).saveProvider(any());
    }

    @Test
    void rejectsInvalidEmailBeforeTouchingRepository() {
        assertThatThrownBy(() -> service.registerProvider(request("Vet Sol", "not-an-email")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(providerRepository, never()).existsByName(anyString());
        verify(providerRepository, never()).saveProvider(any());
    }
}
