package com.petcare.petcarebackend.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Price(BigDecimal value) {

    private static final int MAX_DECIMALS = 2;
    private static final BigDecimal MAX_VALUE = new BigDecimal("99999999.99");

    public Price {
        if (value == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Price must not be negative");
        }
        if (value.stripTrailingZeros().scale() > MAX_DECIMALS) {
            throw new IllegalArgumentException("Price must not have more than " + MAX_DECIMALS + " decimals");
        }
        if (value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Price must not exceed " + MAX_VALUE);
        }
        // Same scale as the database column, so 45000 and 45000.00 are the same price
        value = value.setScale(MAX_DECIMALS, RoundingMode.UNNECESSARY);
    }
}
