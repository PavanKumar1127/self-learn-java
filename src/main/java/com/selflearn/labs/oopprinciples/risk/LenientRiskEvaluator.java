package com.selflearn.labs.oopprinciples.risk;

import com.selflearn.labs.oopprinciples.payment.Money;

/**
 * Always allows (use in internal environments only — illustrates swappable behavior).
 */
public final class LenientRiskEvaluator implements RiskEvaluator {

    @Override
    public boolean allowCharge(String customerId, Money amount) {
        return true;
    }
}
