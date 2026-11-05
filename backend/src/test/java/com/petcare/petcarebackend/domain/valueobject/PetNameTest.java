package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PetNameTest {

    @Test
    void keepsCasingAndCollapsesWhitespace() {
        PetName name = new PetName("  Firu   Lais ");

        assertThat(name.value()).isEqualTo("Firu Lais");
        assertThat(name.normalized()).isEqualTo("firu lais");
    }

    @Test
    void namesDifferingOnlyInCaseOrSpacingShareNormalizedForm() {
        assertThat(new PetName("FIRULAIS").normalized())
                .isEqualTo(new PetName(" firulais ").normalized());
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new PetName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNameLongerThan100Characters() {
        assertThatThrownBy(() -> new PetName("a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
