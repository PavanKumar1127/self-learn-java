# REST API Development — HTTP Semantics & Spring MVC

**Deliverable shaping:** predictable resource modeling + correct status codes improves client reliability and retries.

---

## 1. HTTP methods — intent & idempotency (backend contract)

| Method | Safe? | Typical idempotency | Notes |
|--------|-------|---------------------|-------|
| `GET` | Yes | Yes | Cached; must avoid side-effects |
| `PUT` | No | Intended yes | Whole replacement semantics |
| `PATCH` | No | Maybe | Clarify concurrency (`ETag`/version) |
| `POST` | No | Rarely unless idempotency key | Creates commands |
| `DELETE` | No | Intended yes after success | Repeated delete often `404 vs 204` policy decision |

---

## 2. Status codes engineers must discriminate

| Class | Operational meaning |
|-------|---------------------|
| `2xx` | Success—still inspect body for logical errors (`200` OK with `{ errorCode }`). |
| `400` Bad Request | malformed or rule violation caller can fix |
| `401` Unauthorized | authenticate |
| `403` Forbidden | authenticated but not allowed |
| `404` Not Found | resource identity missing |
| `409` Conflict | concurrency / uniqueness violation |
| `422` Unprocessable Entity | validation failure (common JSON APIs) |
| `429` Too Many Requests | apply backoff / respect `Retry-After` |
| `5xx` | server-side—retry only if safe + bounded |

---

## 3. Request lifecycle (simplified)

```text
Client → Tomcat accept thread → Servlet filter chain (security/logging)
      → DispatcherServlet → HandlerMapping → Controller method
      → Return value → HttpMessageConverter (JSON) → Response
```

**Implication:** anything **before** controller (filters) is where auth & trace IDs often attach.

---

## 4. Controller example — CRUD + pagination

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable UUID id) {
        return users.get(id);
    }

    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") @Max(100) int size
    ) {
        return users.list(page, size);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body) {
        UserResponse created = users.create(body);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/v1/users/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public UserResponse replace(@PathVariable UUID id, @Valid @RequestBody ReplaceUserRequest body) {
        return users.replace(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        users.delete(id);
    }
}
```

**Pagination real-world:**

- Bounded `size` (**never unbounded**) defends CPU/DB/memory.
- Use cursor pagination for churning timelines—classic offset paging drifts heavily under concurrent inserts (`Keyset` paging).

---

## 5. Serialization / deserialization pitfalls

Jackson maps JSON fields ⇄ Java types unless configured otherwise.

Problem cases:

| Issue | Guidance |
|-------|----------|
| `LocalDateTime` ambiguity | Prefer **ISO-8601** UTC (`Instant` outward) unless domain demands local wall clock explicitly |
| `Optional` fields | Avoid—they serialize awkwardly (`"present":false` wrappers) unless custom module |
| Security | disable **polymorphic default typing**; validate unknown properties policy (`FAIL_ON_UNKNOWN_PROPERTIES`) |

Controlled DTO layering:

```
JSON -> Request DTO -> Domain model -> Persisted Entity
```

Expose **responses** trimmed (no leakage of internal FK graph).

---

## 6. API versioning realities

Popular patterns:

```
/api/v1/...                   # URI prefix (explicit, cache friendly)
Accept: application/vnd.x.v2+json  # negotiation (harder caches)
```

**Choose one** per organizational standard—stay consistent gateway-wide.
