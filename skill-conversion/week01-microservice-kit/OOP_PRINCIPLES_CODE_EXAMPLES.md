# OOP principles — runnable code map

**Package:** `src/main/java/com/selflearn/labs/oopprinciples/`

**Run:** `./gradlew runOopPrinciplesDemo` or `./gradlew run --args oop`

| Principle | Classes | What to read first |
|-----------|---------|---------------------|
| **Encapsulation** | `encapsulation.Order`, `OrderStatus`, `OrderId` | `Order.complete()`, `submit()`, `cancel()` — no public setters on status |
| **Abstraction** | `payment.PaymentGateway`, `StripePaymentGateway`, `MockPaymentGateway` | Callers use `PaymentGateway` only |
| **Polymorphism** | `risk.RiskEvaluator`, `ConservativeRiskEvaluator`, `LenientRiskEvaluator` | Same interface, different behavior plugged into `OrderService` |
| **Composition** | `OrderService` + `persistence.OrderRepository` | Constructor-injected collaborators; service **does not extend** repository |
| **Anti-pattern** | `antipattern.InheritanceTowerAntiPattern` | Javadoc-only smell — do not copy |

Cross-reference theory: `week01-microservice-kit/01-java-foundations-before-spring.md` §1–2.
