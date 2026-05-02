package com.selflearn.labs.oopprinciples.risk;

import com.selflearn.labs.oopprinciples.payment.Money;

/**
 * <strong>Polymorphism:</strong> multiple concrete evaluators implement the same contract; {@link com.selflearn.labs.oopprinciples.OrderService}
 * can iterate or choose strategy without {@code if (type == ...)} chains growing without bound.
 */
public interface RiskEvaluator {

    /**
     * @return {@code true} if the order should be allowed to proceed to payment
     */
    boolean allowCharge(String customerId, Money amount);
}
