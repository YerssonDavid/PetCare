package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferingNameTest {

    @Test
    void keepsCasingAndCollapsesWhitespace() {
        OfferingName name = new OfferingName("  Baño   Completo ");

        assertThat(name.value()).isEqualTo("Baño Completo");
        assertThat(name.normalized()).isEqualTo("baño completo");
    }

    @Test
    void namesDifferingOnlyInCaseOrSpacingShareNormalizedForm() {
        assertThat(new OfferingName("BAÑO COMPLETO").normalized())
                .isEqualTo(new OfferingName(" baño  completo ").normalized());
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new OfferingName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> new OfferingName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNameLongerThanMaximum() {
        assertThatThrownBy(() -> new OfferingName("a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
