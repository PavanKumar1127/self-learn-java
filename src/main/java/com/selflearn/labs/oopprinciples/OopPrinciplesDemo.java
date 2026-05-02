package com.selflearn.labs.oopprinciples;

import com.selflearn.labs.oopprinciples.encapsulation.Order;
import com.selflearn.labs.oopprinciples.encapsulation.OrderId;
import com.selflearn.labs.oopprinciples.payment.MockPaymentGateway;
import com.selflearn.labs.oopprinciples.payment.Money;
import com.selflearn.labs.oopprinciples.payment.StripePaymentGateway;
import com.selflearn.labs.oopprinciples.persistence.InMemoryOrderRepository;
import com.selflearn.labs.oopprinciples.risk.ConservativeRiskEvaluator;
import com.selflearn.labs.oopprinciples.risk.LenientRiskEvaluator;

/**
 * Runnable demo: encapsulation invariants, polymorphic risk evaluators, swappable payment gateways, composition in
 * {@link OrderService}.
 *
 * <p>Run: {@code ./gradlew runOopPrinciplesDemo}
 */
public final class OopPrinciplesDemo {

    public static void main(String[] args) {
        System.out.println("=== Encapsulation: Order.complete() guards ===");
        Order draft = new Order(OrderId.random(), 500);
        draft.submit();
        draft.complete();
        System.out.println("Happy path after submit: status=" + draft.status());
        Order bad = new Order(OrderId.random(), 0);
        bad.submit();
        try {
            bad.complete();
        } catch (IllegalStateException expected) {
            System.out.println("Expected failure: " + expected.getMessage());
        }

        System.out.println("\n=== Composition + abstraction: same OrderService, Mock gateway ===");
        var repo = new InMemoryOrderRepository();
        OrderId id = OrderId.random();
        Order okOrder = new Order(id, 1_200);
        repo.save(okOrder);

        OrderService withMock = new OrderService(
                repo,
                new MockPaymentGateway(true),
                new LenientRiskEvaluator()
        );
        Order paid = withMock.checkout(id, "cust-1");
        System.out.println("Paid order status=" + paid.status());

        System.out.println("\n=== Polymorphism: ConservativeRiskEvaluator blocks demo- large order ===");
        OrderId id2 = OrderId.random();
        Order big = new Order(id2, 500_000);
        repo.save(big);
        OrderService strict = new OrderService(
                repo,
                new MockPaymentGateway(true),
                new ConservativeRiskEvaluator(10_000)
        );
        try {
            strict.checkout(id2, "demo-user");
        } catch (IllegalStateException e) {
            System.out.println("Expected risk block: " + e.getMessage());
        }

        System.out.println("\n=== Abstraction: StripePaymentGateway behind PaymentGateway interface ===");
        OrderId id3 = OrderId.random();
        Order stripeOrder = new Order(id3, 999);
        repo.save(stripeOrder);
        OrderService stripePath = new OrderService(
                repo,
                new StripePaymentGateway("sk_test_suffix"),
                new LenientRiskEvaluator()
        );
        try {
            Order done = stripePath.checkout(id3, "prod-user");
            System.out.println("Stripe path result status=" + done.status());
        } catch (IllegalStateException payFail) {
            System.out.println("Simulated decline: " + payFail.getMessage());
        }

        System.out.println("\n=== Anti-pattern reference: see antipattern.InheritanceTowerAntiPattern (javadoc) ===");
    }
}
