package com.selflearn.labs.oopprinciples.risk;

import com.selflearn.labs.oopprinciples.payment.Money;

/**
 * Blocks high-value orders for demo customers (simulated fraud rule).
 */
public final class ConservativeRiskEvaluator implements RiskEvaluator {

    private final long maxCentsForDemoCustomer;

    public ConservativeRiskEvaluator(long maxCentsForDemoCustomer) {
        this.maxCentsForDemoCustomer = maxCentsForDemoCustomer;
    }

    @Override
    public boolean allowCharge(String customerId, Money amount) {
        if (customerId != null && customerId.startsWith("demo-") && amount.cents() > maxCentsForDemoCustomer) {
            return false;
        }
        return true;
    }
}
