package com.selflearn.labs.oopprinciples;

import com.selflearn.labs.oopprinciples.encapsulation.Order;
import com.selflearn.labs.oopprinciples.encapsulation.OrderId;
import com.selflearn.labs.oopprinciples.payment.Money;
import com.selflearn.labs.oopprinciples.payment.PaymentGateway;
import com.selflearn.labs.oopprinciples.payment.PaymentResult;
import com.selflearn.labs.oopprinciples.persistence.OrderRepository;
import com.selflearn.labs.oopprinciples.risk.RiskEvaluator;

/**
 * <strong>Composition:</strong> the service <em>has</em> collaborators (repository, gateway, risk) injected via
 * constructor — it does not extend them. Favors testing and clear boundaries vs a deep inheritance tower.
 */
public final class OrderService {

    private final OrderRepository orders;
    private final PaymentGateway payments;
    private final RiskEvaluator risk;

    public OrderService(OrderRepository orders, PaymentGateway payments, RiskEvaluator risk) {
        this.orders = orders;
        this.payments = payments;
        this.risk = risk;
    }

    /**
     * Orchestrates submit → risk → charge → complete. Transaction boundary in real Spring would wrap this method.
     */
    public Order checkout(OrderId id, String customerId) {
        Order order = orders.findById(id).orElseThrow(() -> new IllegalArgumentException("Unknown order " + id));
        order.submit();
        orders.save(order);

        Money amount = new Money(order.totalCents());
        if (!risk.allowCharge(customerId, amount)) {
            throw new IllegalStateException("Risk check failed for customer " + customerId);
        }

        PaymentResult pay = payments.charge(id.value().toString(), amount);
        if (!pay.approved()) {
            throw new IllegalStateException("Payment declined: " + pay.processorReference());
        }

        order.complete();
        return orders.save(order);
    }
}
