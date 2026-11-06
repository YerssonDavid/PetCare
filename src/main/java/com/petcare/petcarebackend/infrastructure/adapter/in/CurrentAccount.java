package com.petcare.petcarebackend.infrastructure.adapter.in;

import java.security.Principal;
import java.util.UUID;

// The subject of a validated token is the account ID, so the acting account never comes from the request
final class CurrentAccount {

    private CurrentAccount() {
    }

    static UUID idOf(Principal principal) {
        return UUID.fromString(principal.getName());
    }
}
