package com.selflearn.labs.oopprinciples.encapsulation;

/**
 * Order lifecycle states. Kept public for persistence / API mapping; transitions are enforced on {@link Order}.
 */
public enum OrderStatus {
    DRAFT,
    SUBMITTED,
    COMPLETED,
    CANCELLED
}
