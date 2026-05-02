package com.selflearn.labs.oopprinciples.encapsulation;

import java.util.Objects;

/**
 * <strong>Encapsulation:</strong> mutable state ({@code status}, {@code totalCents}) is not exposed for arbitrary
 * mutation. Callers use intention-revealing methods that <em>validate invariants</em> before transitions.
 *
 * <p>Production meaning: illegal states (e.g. {@code COMPLETED} → {@code DRAFT}) cannot be forced by a stray
 * setter from another layer.
 */
public final class Order {

    private final OrderId id;
    private OrderStatus status;
    private long totalCents;

    public Order(OrderId id, long totalCents) {
        this.id = Objects.requireNonNull(id, "id");
        if (totalCents < 0) {
            throw new IllegalArgumentException("totalCents must be non-negative");
        }
        this.status = OrderStatus.DRAFT;
        this.totalCents = totalCents;
    }

    public OrderId id() {
        return id;
    }

    /**
     * Read-only view of status; no setter — transitions go through domain methods.
     */
    public OrderStatus status() {
        return status;
    }

    public long totalCents() {
        return totalCents;
    }

    /**
     * Legal transition: {@code DRAFT} → {@code SUBMITTED}.
     */
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT orders can be submitted, was " + status);
        }
        this.status = OrderStatus.SUBMITTED;
    }

    /**
     * Completes the order after payment capture. Rejects illegal transitions and zero-total edge policy.
     */
    public void complete() {
        if (status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED orders can be completed, was " + status);
        }
        if (totalCents <= 0) {
            throw new IllegalStateException("Cannot complete order with non-positive total");
        }
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * Cancels from states where cancellation is still a valid business outcome.
     */
    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Package-private or service-only adjustment if business rules allow (example: line-item correction before submit).
     * Still guarded — not a public setter.
     */
    void adjustTotalBeforeSubmit(long newTotalCents) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Total can only be adjusted in DRAFT");
        }
        if (newTotalCents < 0) {
            throw new IllegalArgumentException("newTotalCents must be non-negative");
        }
        this.totalCents = newTotalCents;
    }
}
