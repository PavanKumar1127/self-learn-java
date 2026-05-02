# Spring Core — Dependency Injection, IoC & Bean Lifecycle

**Goal:** know how Spring wires objects so you debug misconfiguration confidently.

---

## 1. Dependency Injection vs manual `new`

**Without Spring:**

```java
var repo = new JdbcOrderRepository(ds);
var service = new OrderService(repo); // orchestration coupling done by hand
```

**With Spring:**

- Spring **instantiates singletons once** per application context (`ApplicationContext`)
- Dependencies satisfied through **constructor parameters** (**preferred**) or setter/field (**discouraged in modern codebases**).

```java
@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order placeOrder(PlaceOrderCommand cmd) {
        // ...
        return orders.save(Order.from(cmd));
    }
}
```

**Why constructors:** fields are **`final`** (immutable collaborators), mocks inject trivially.

---

## 2. IoC Container mental model

`ApplicationContext`:

- Parses configuration classes & component scan
- Builds **dependency graph**, detects circular dependencies (**fail-fast** ideally)
- Manages **`@Bean` singleton scope** lifecycle (`@Bean` defaults singleton unless annotated otherwise)

**Common scopes:**

| Scope | When |
|-------|------|
| `singleton` (default bean) | One instance per Spring context |
| `prototype` | New instance **each lookup** injection—use sparingly (resource cleanup complexity) |

Web scopes (`request`/`session`): require web context proxies—understand leakage risks.

---

## 3. Component scanning stereotypes

```java
@Component   // generic Spring-managed bean
@Service     // annotated @Component — semantic label for domain service layer
@Repository  // @Component — often adds exception translation semantics for data access historically
@RestController // @Controller + convenience for REST semantics
@Configuration // source of `@Bean` factory methods — proxy subclass for `@Bean` intra-method injection
```

**Package layout example:**

```
com.example.orders
 ├── web.OrderController           (@RestController)
 ├── service.OrderService          (@Service)
 ├── repository.OrderRepository    (interface Spring Data implements)
 └── model.Order                   (pure domain / JPA entity — not component)
```

---

## 4. `@Bean` factories vs `@Component` classes

**Class-based component:**

```java
@Service
public class PricingService {
    ...
}
```

**Factory bean** (explicit construction when integrating third-party libraries):

```java
@Configuration
public class MessagingConfig {

    @Bean
    ObjectMapper kafkaObjectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }
}
```

**Golden rule:**

- Annotate domain classes with stereotypes when Spring should own lifecycle.
- Use `@Bean` when **you configure** immutable third-party constructs.

---

## 5. Bean lifecycle hooks (minimal set)

Implement when you allocate external resources (**close on shutdown gracefully**):

```java
@Service
public class WarmCacheService implements InitializingBean, DisposableBean {
    private final ExternalCatalogClient client;

    public WarmCacheService(ExternalCatalogClient client) {
        this.client = client;
    }

    @Override
    public void afterPropertiesSet() {
        // warm minimal dataset (guard with feature flag toggles!)
    }

    @Override
    public void destroy() throws Exception {
        // release connections if owning client resources
    }
}
```

Prefer **`@PostConstruct` / `@PreDestroy`** (`jakarta.annotation`) for readability if allowed by your stack guidelines.

---

## 6. Real-world pitfalls

| Problem | Cause | Fix orientation |
|---------|-------|-----------------|
| `UnsatisfiedDependencyException` | missing bean implementing interface | annotate impl or provide `@Bean` |
| Duplicate bean | two `@Service` implementations of same primary type without `@Qualifier` | split profiles or annotate `@Primary` **only deliberately** |
| Circular dependency (`A→B→A`) | cyclic constructor graph | refactor domain boundaries or laziness (last resort `@Lazy`) |
| Tests failing only in CI | component scan excludes test packages incorrectly | `@Import` explicit config slices |

---

## 7. Optional: Conditional beans (feature toggles)

```java
@Bean
@ConditionalOnProperty(prefix = "app.cache", name = "enabled", havingValue = "true")
public WarmCacheExecutor warmExecutor() {
    return new WarmCacheExecutor();
}
```

Keeps heavyweight beans off when disabled—reduces container startup variance.
