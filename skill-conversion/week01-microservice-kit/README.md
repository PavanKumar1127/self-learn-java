# Week 1 — Microservice Kit (reference library)

Standalone guides you can open in IntelliJ/Cursor beside your code. Aligns with **Week 1 goal**: CRUD Spring Boot microservice **+ Docker + DB + secure config + HLD sketch + DSA**.

**Estimated study load in your pasted plan:** ~10 h/day × 7 days — these docs compress **patterns** so you spend time **building**, not googling trivia.

---

If you follow the **Day 1–7** schedule from your study plan — see **`DAY_BY_DAY_WEEK1_MAP.md`**.

## Reading order (suggested)

| # | Doc | Purpose |
|---|-----|---------|
| 1 | [`01-java-foundations-before-spring.md`](01-java-foundations-before-spring.md) | Language refresh: OOP, generics, collections, streams, exceptions, annotations, reflection pitfalls |
| 2 | [`02-spring-dependency-injection-and-beans.md`](02-spring-dependency-injection-and-beans.md) | IoC/DI, lifecycles, stereotype annotations, `@Configuration`/`@Bean` trade-offs |
| 3 | [`03-spring-boot-configuration-and-starters.md`](03-spring-boot-configuration-and-starters.md) | Auto-config, YAML, profiles, `@ConfigurationProperties` |
| 4 | [`04-rest-apis-http-and-mapping.md`](04-rest-apis-http-and-mapping.md) | REST semantics, MVC flow, serialization, pagination basics |
| 5 | [`05-bean-validation.md`](05-bean-validation.md) | Jakarta Validation + custom validators + groups + nested `@Valid` |
| 6 | [`06-exception-handling-and-error-model.md`](06-exception-handling-and-error-model.md) | `@ControllerAdvice`, problem+json-style bodies, RFC 7807 spirit |
| 7 | [`07-logging-slf4j-mdc-structured-json.md`](07-logging-slf4j-mdc-structured-json.md) | SLF4J, Logback patterns, correlation IDs via MDC, JSON logs |
| 8 | [`08-docker-for-spring-boot.md`](08-docker-for-spring-boot.md) | Images vs containers, Compose, multi-stage, non-root, health checks |
| 9 | [`09-relational-database-design-and-flyway.md`](09-relational-database-design-and-flyway.md) | Modeling, migrations, indexing, transactional thinking |
| 10 | [`10-security-tls-secrets-encryption.md`](10-security-tls-secrets-encryption.md) | TLS, secret stores, hashing vs encryption, Spring config hygiene |
| 11 | [`11-system-design-user-management-service.md`](11-system-design-user-management-service.md) | First HLD: Gateway, auth, caching, scaling, failures |
| 12 | [`12-dsa-arrays-hashmaps-week1.md`](12-dsa-arrays-hashmaps-week1.md) | Prefix sums, sliding window, hashing patterns + curated problems |
| — | [`OOP_PRINCIPLES_CODE_EXAMPLES.md`](OOP_PRINCIPLES_CODE_EXAMPLES.md) | **Runnable** OOP lab: `com.selflearn.labs.oopprinciples` (`./gradlew runOopPrinciplesDemo`) |

---

## Repos you can extend

- Gradle app root: `/Users/apple/Development/JAVA/self-learn` (Java 25 toolchain). Consider adding **`spring-boot-starter-*`** deps when you bootstrap your Week 1 service (separate branch or submodule if you prefer).

---

## Deliverables checklist (mirror your Week 1 spec)

| Artifact | Minimum bar |
|-----------|--------------|
| CRUD REST | Stable resource URLs + correct status codes |
| Validation | Fail fast with standardized error shape |
| Exception handler | Maps domain faults → HTTP + logs context |
| Docker | Multi-stage image, non-root user, HEALTHCHECK aligned with actuator |
| DB | PostgreSQL-compatible schema + Flyway `V*` migrations |
| Secrets | `.env`/runtime injection only — **never Git** |
| HLD diagram | Boxes + arrows + failure story + bottleneck |
| DSA | 10 problems tracked in your notebook |

---

## External authoritative sources (when docs disagree)

- [Spring Boot reference](https://docs.spring.io/spring-boot/reference/index.html)
- [Docker Docs](https://docs.docker.com/)
- [PostgreSQL docs](https://www.postgresql.org/docs/)
- [OWASP Cheat Sheets](https://cheatsheetseries.owasp.org/)
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/3.1/jakarta-bean-validation-spec-3.1.html)
