package com.selflearn.labs.oopprinciples.antipattern;

/**
 * <strong>Anti-pattern (documented, not for reuse):</strong> deep inheritance that merges controller, service,
 * and persistence concerns into one fragile tower. New behavior requires subclassing; tests cannot swap a mock
 * repository without subclass gymnastics; every layer change risks breaking unrelated features.
 *
 * <p><strong>Production meaning:</strong> prefer {@linkplain com.selflearn.labs.oopprinciples.OrderService composition}
 * and small interfaces ({@code PaymentGateway}, {@code OrderRepository}) over “God base classes”.
 *
 * <p>This file exists only as a <em>smell catalog</em> — do not extend further in real codebases.
 */
public abstract class InheritanceTowerAntiPattern extends MegaServiceBase {

    protected InheritanceTowerAntiPattern() {
        super();
    }

    // Imagine more template methods forcing subclasses to know HTTP + SQL + Stripe...
}

abstract class MegaServiceBase extends MegaRepositoryBase {

    protected MegaServiceBase() {
        super();
    }

    protected final void logAndExecute(String sql) {
        // buried side effects — hard to test
    }
}

abstract class MegaRepositoryBase {

    protected MegaRepositoryBase() {
    }

    protected final Object queryOne(String sql) {
        return null;
    }
}
