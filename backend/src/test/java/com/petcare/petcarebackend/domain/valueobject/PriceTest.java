package com.petcare.petcarebackend.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceTest {

    @Test
    void acceptsZero() {
        assertThat(new Price(BigDecimal.ZERO).value()).isEqualByComparingTo("0");
    }

    @Test
    void normalizesToTwoDecimals() {
        assertThat(new Price(new BigDecimal("45000")))
                .isEqualTo(new Price(new BigDecimal("45000.000")));
        assertThat(new Price(new BigDecimal("45000")).value().scale()).isEqualTo(2);
    }

    @Test
    void acceptsMaximumValue() {
        assertThat(new Price(new BigDecimal("99999999.99")).value()).isEqualByComparingTo("99999999.99");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Price(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegative() {
        assertThatThrownBy(() -> new Price(new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatThrownBy(() -> new Price(new BigDecimal("10.005")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsValueAboveMaximum() {
        assertThatThrownBy(() -> new Price(new BigDecimal("100000000")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
