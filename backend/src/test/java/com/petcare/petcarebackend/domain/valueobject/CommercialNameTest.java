package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommercialNameTest {

    @Test
    void keepsCasingAndCollapsesWhitespace() {
        CommercialName name = new CommercialName("  Vet   Sol ");

        assertThat(name.value()).isEqualTo("Vet Sol");
        assertThat(name.normalized()).isEqualTo("vet sol");
    }

    @Test
    void namesDifferingOnlyInCaseShareNormalizedForm() {
        assertThat(new CommercialName("HFjj").normalized())
                .isEqualTo(new CommercialName("Hfjj").normalized());
    }

    @Test
    void innerSpaceMakesADifferentName() {
        assertThat(new CommercialName("Hf jj").normalized())
                .isNotEqualTo(new CommercialName("Hfjj").normalized());
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new CommercialName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNameLongerThan100Characters() {
        assertThatThrownBy(() -> new CommercialName("a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
