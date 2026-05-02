package com.selflearn.labs.oopprinciples.payment;

/**
 * <strong>Abstraction:</strong> callers depend on this contract, not on Stripe SDK types or test harness details.
 * Swap {@link StripePaymentGateway} in prod and {@link MockPaymentGateway} in tests without changing
 * {@link com.selflearn.labs.oopprinciples.OrderService}.
 */
public interface PaymentGateway {

    /**
     * Authorize or capture a charge for the given order id and amount (simplified single-step model for demo).
     */
    PaymentResult charge(String orderId, Money amount);
}
