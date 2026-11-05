package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneTest {

    @Test
    void stripsSeparatorsAndKeepsLeadingPlus() {
        assertThat(new Phone("+57 (311) 111-1111").value()).isEqualTo("+573111111111");
    }

    @Test
    void rejectsBlankPhone() {
        assertThatThrownBy(() -> new Phone("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFewerThanSevenDigits() {
        assertThatThrownBy(() -> new Phone("123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMoreThanFifteenDigits() {
        assertThatThrownBy(() -> new Phone("1234567890123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLetters() {
        assertThatThrownBy(() -> new Phone("311abc1111"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
