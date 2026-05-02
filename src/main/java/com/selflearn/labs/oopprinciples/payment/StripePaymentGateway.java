package com.selflearn.labs.oopprinciples.payment;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Production-shaped implementation (no real Stripe SDK — simulates network + id generation).
 */
public final class StripePaymentGateway implements PaymentGateway {

    private final String apiKeySuffix;

    public StripePaymentGateway(String apiKeySuffix) {
        if (apiKeySuffix == null || apiKeySuffix.isBlank()) {
            throw new IllegalArgumentException("apiKeySuffix required for demo");
        }
        this.apiKeySuffix = apiKeySuffix;
    }

    @Override
    public PaymentResult charge(String orderId, Money amount) {
        // Simulate rare decline
        boolean approved = ThreadLocalRandom.current().nextDouble() > 0.05;
        String ref = approved ? "ch_stripe_" + orderId + "_" + apiKeySuffix.hashCode() : "declined";
        return new PaymentResult(approved, ref);
    }
}
