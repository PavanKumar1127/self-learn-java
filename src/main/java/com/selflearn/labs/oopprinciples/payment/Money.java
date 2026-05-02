package com.selflearn.labs.oopprinciples.payment;

/**
 * Money as minor units (cents) to avoid floating-point in payments code paths.
 */
public record Money(long cents) {

    public Money {
        if (cents < 0) {
            throw new IllegalArgumentException("cents must be non-negative");
        }
    }
}
