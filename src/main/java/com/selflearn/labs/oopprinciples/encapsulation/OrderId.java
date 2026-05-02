package com.selflearn.labs.oopprinciples.encapsulation;

import java.util.UUID;

/**
 * Type-safe identifier (value object). Prevents mixing UUIDs for unrelated aggregates.
 */
public record OrderId(UUID value) {

    public static OrderId random() {
        return new OrderId(UUID.randomUUID());
    }
}
