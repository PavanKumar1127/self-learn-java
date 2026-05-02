package com.selflearn.labs.oopprinciples.payment;

/**
 * Test double: deterministic, fast, no network. Same {@link PaymentGateway} contract as {@link StripePaymentGateway}.
 */
public final class MockPaymentGateway implements PaymentGateway {

    private final boolean approve;

    public MockPaymentGateway(boolean approve) {
        this.approve = approve;
    }

    @Override
    public PaymentResult charge(String orderId, Money amount) {
        return new PaymentResult(approve, approve ? "mock_ch_" + orderId : "mock_declined");
    }
}
