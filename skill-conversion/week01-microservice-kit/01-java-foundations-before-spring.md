# Java Foundations Before Spring (Week 1)

Spring is **heavy use of generics, proxies, annotations, and exceptions**. Brush these concepts so `@Component`/`@Transactional` internals do not feel like magic.

---

## 1. OOP principles (engineering lens)

| Principle | Production meaning |
|-----------|---------------------|
| **Encapsulation** | Hide mutable state behind methods; validates invariants (`Order.complete()` rejects illegal transitions). |
| **Polymorphism** | Substitute implementations (`PaymentProcessor`) without changing callers. |
| **Abstraction** | Depend on **`PaymentGateway` interface**, swap Stripe/MockTest in tests. |
| **Composition** | Prefer assembling services through **constructor injection** over deep inheritance hierarchies (`OrderService` *has* repositories, rarely *is* repository). |

**Anti-pattern:** `BaseController extends BaseService extends BaseRepository extends BaseEverything` → untestable and fragile.

---

## 2. Interfaces vs abstract classes

```java
// Interface: capability contract across unrelated types — multiple inheritance substitute
public interface PaymentGateway {
    AuthorizeResult authorize(Money amount, CardToken token);
}

// Abstract class: shared partial behavior + state template (use sparingly in services)
public abstract class AbstractTimedJob {
    public final void runSafely() {
        long start = System.nanoTime();
        try {
            execute();
        } finally {
            long ms = (System.nanoTime() - start) / 1_000_000L;
            Metrics.recordLatency(getClass(), ms); // illustrative
        }
    }

    protected abstract void execute();
}
```

**Rule of thumb:**

- **`interface`** for cross-cutting behaviors and **dependency inversion**
- **`abstract`** when subclasses share identical control flow skeleton

---

## 3. Generics — what Spring assumes you know

**Type erasure:** generic type parameters disappear at runtime (except reflections on method signatures/decls).

```java
public interface CrudRepository<T, ID> {
    Optional<T> findById(ID id);
}

// Wildcards for API flexibility
public void process(List<? extends User> viewers) { ... } // read-only safe
```

** bounded type parameters**:

```java
public <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}
```

**Why it bites:** deserialization (`ObjectMapper`), `ParameterizedTypeReference` in Spring's `RestTemplate/WebClient`, generics on proxied `@Transactional` interfaces.

---

## 4. Collections Framework — correctness & Big-O intuition

| Type | Typical use |
|------|-------------|
| `ArrayList` | Random access sequences (default mutable list). |
| `LinkedList` | Rare in backend services (cache-unfriendly traversal). |
| `HashMap` / `HashSet` | **`O(1)` average lookups** keyed by IDs. |
| `TreeMap` / `TreeSet` | Sorted scans at `O(log n)` cost vs hash. |

**Thread-safety:**

- **`ConcurrentHashMap`** for caches/registries mutated by concurrent requests.
- **Never mutate** plain `HashMap` from concurrent threads unless synchronized externally.

---

## 5. Streams API — when/when-not

Streams shine for **immutable transforms** inside one thread.

```java
List<String> normalized = ids.stream()
    .filter(s -> !s.isBlank())
    .map(String::trim)
    .distinct()
    .sorted()
    .toList(); // immutable list (since Java 16)
```

**Do not:**

- Hide **I/O**, **DB**, or blocking calls inside pipelines without control.
- Use parallel streams (`parallelStream`) on trivial batch sizes—it can degrade predictability due to **`ForkJoinPool` contention**.

---

## 6. `Optional` — return contract

```java
public Optional<Order> findOrder(OrderId id) {
    return storage.get(id); // callers decide absence handling
}

// Prefer not to misuse:
public Optional<String> silly(String s) {
    return Optional.ofNullable(s).filter(x -> true); // noise
}
```

**Best practice:**

- **`Optional`** as **method return**, not pervasive field/storage type.
- **Never** serialize `Optional` from REST JSON by default—it confuses consumers.

---

## 7. Exception hierarchy

```text
Throwable
├── Error               (OutOfMemoryError — rarely catch)
└── Exception
    ├── IOException     (often checked environmental failure)
    ├── SQLException    ( JDBC — usually wrapped/unwrapped)
    └── RuntimeException
        └── IllegalArgumentException, IllegalStateException, ...
```

**Spring HTTP mapping:** unchecked domain exceptions bubble to **`@ControllerAdvice`**; translate to HTTP **4xx/5xx** deliberately.

Checked exceptions through entire service stack often become leaky—many teams migrate to unchecked **domain faults** documented in APIs.

---

## 8. Annotations basics (before Spring overlays them)

Retention:

- `SOURCE`: compile-time tooling only (`@Override`).
- `CLASS`: bytecode present, unavailable at runtime.
- `RUNTIME`: visible via **`Reflection`** (Spring scans these).

Annotations drive:

- Persistence mapping (Jakarta Persistence)
- Validation (`@NotBlank`)
- Security (`PreAuthorize`)

---

## 9. Reflection — power & peril

Reflection lets frameworks discover methods/fields annotated with markers.

```java
Method m = MyService.class.getMethod("dangerous", String.class);
m.invoke(instance, payload); // breaks visibility if setAccessible(true) — brittle
```

**Production caution:**

- **Performance cost** scanning huge classpaths.
- **`setAccessible` on JDK classes** forbidden by JPMS without `--add-opens` (why your HashMap labs used JVM flags).

**Prefer:** annotated beans + constructors over reflection hacks outside frameworks.

---

## 10. Mini-pattern: immutability for DTO/command objects

Records (Java **16+**) keep API contracts transparent:

```java
public record CreateUserCommand(@NotBlank String email, Locale preferredLocale) { }
```

**Why:** concurrency reasoning + fewer accidental setter mutations during validation layers.
