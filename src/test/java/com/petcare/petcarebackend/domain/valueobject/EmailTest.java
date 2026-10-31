package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void trimsAndLowercases() {
        assertThat(new Email("  Ana.Perez@Mail.COM ").value()).isEqualTo("ana.perez@mail.com");
    }

    @Test
    void rejectsBlankEmail() {
        assertThatThrownBy(() -> new Email("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> new Email("ana@mail"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmailLongerThan150Characters() {
        assertThatThrownBy(() -> new Email("a".repeat(142) + "@mail.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsEmailOfExactly150Characters() {
        assertThat(new Email("a".repeat(141) + "@mail.com").value()).hasSize(150);
    }
}
