package com.petcare.petcarebackend.infrastructure.adapter;

import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUsersAdapterTest {

    private static final String EMAIL = "ana@mail.com";
    private static final UUID ID = UUID.randomUUID();

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ProviderRepository providerRepository;

    private LoginUsersAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LoginUsersAdapter(ownerRepository, providerRepository);
    }

    private OwnerJpa owner(boolean active) {
        OwnerJpa owner = new OwnerJpa();
        owner.setId(ID);
        owner.setPasswordHash("owner-hash");
        owner.setRole(Role.USER);
        owner.setActiveAccount(active);
        return owner;
    }

    private ProviderJpa provider() {
        ProviderJpa provider = new ProviderJpa();
        provider.setId(ID);
        provider.setPasswordHash("provider-hash");
        provider.setRole(Role.PROVIDER);
        return provider;
    }

    @Test
    void findsOwnerWithoutQueryingProviders() {
        when(ownerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner(true)));

        Optional<LoginAccount> account = adapter.findByEmail(EMAIL);

        assertThat(account).contains(new LoginAccount(ID, "owner-hash", Role.USER, true));
        verify(providerRepository, never()).findByEmail(any());
    }

    @Test
    void keepsTheInactiveStateOfAnOwner() {
        when(ownerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner(false)));

        assertThat(adapter.findByEmail(EMAIL)).contains(new LoginAccount(ID, "owner-hash", Role.USER, false));
    }

    @Test
    void fallsBackToProviderAlwaysActive() {
        when(ownerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(providerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(provider()));

        assertThat(adapter.findByEmail(EMAIL)).contains(new LoginAccount(ID, "provider-hash", Role.PROVIDER, true));
    }

    @Test
    void isEmptyWhenTheEmailIsInNeitherTable() {
        when(ownerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(providerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThat(adapter.findByEmail(EMAIL)).isEmpty();
    }

    @Test
    void findsAnOwnerByIdWithoutQueryingProviders() {
        when(ownerRepository.findById(ID)).thenReturn(Optional.of(owner(false)));

        assertThat(adapter.findById(ID)).contains(new LoginAccount(ID, "owner-hash", Role.USER, false));
        verify(providerRepository, never()).findById(any());
    }

    @Test
    void findsAProviderByIdWhenNoOwnerHasIt() {
        when(ownerRepository.findById(ID)).thenReturn(Optional.empty());
        when(providerRepository.findById(ID)).thenReturn(Optional.of(provider()));

        assertThat(adapter.findById(ID)).contains(new LoginAccount(ID, "provider-hash", Role.PROVIDER, true));
    }

    @Test
    void isEmptyWhenTheIdIsInNeitherTable() {
        when(ownerRepository.findById(ID)).thenReturn(Optional.empty());
        when(providerRepository.findById(ID)).thenReturn(Optional.empty());

        assertThat(adapter.findById(ID)).isEmpty();
    }
}
