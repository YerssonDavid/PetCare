package com.petcare.petcarebackend.infrastructure.adapter;

import com.petcare.petcarebackend.application.dto.LoginAccount;
import com.petcare.petcarebackend.application.port.out.login.LoginUsersPort;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OwnerRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.entity.OwnerJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginUsersAdapter implements LoginUsersPort {

    private final OwnerRepository ownerRepository;
    private final ProviderRepository providerRepository;

    @Override
    public Optional<LoginAccount> findByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .map(LoginUsersAdapter::fromOwner)
                .or(() -> providerRepository.findByEmail(email).map(LoginUsersAdapter::fromProvider));
    }

    @Override
    public Optional<LoginAccount> findById(UUID id) {
        return ownerRepository.findById(id)
                .map(LoginUsersAdapter::fromOwner)
                .or(() -> providerRepository.findById(id).map(LoginUsersAdapter::fromProvider));
    }

    private static LoginAccount fromOwner(OwnerJpa owner) {
        return new LoginAccount(owner.getId(), owner.getPasswordHash(), owner.getRole(), owner.getActiveAccount());
    }

    // Providers have no inactive state, so they can always log in
    private static LoginAccount fromProvider(ProviderJpa provider) {
        return new LoginAccount(provider.getId(), provider.getPasswordHash(), provider.getRole(), true);
    }
}
