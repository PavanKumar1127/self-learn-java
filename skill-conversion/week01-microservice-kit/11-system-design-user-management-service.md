# System Design — User Management Microservice (First HLD)

Scope Week 1: **readable boxes + coherent data ownership + pragmatic capacity story**—not FAANG caricature arithmetic without grounding.

---

## 1. Problem statement snapshot

Expose APIs:

- Register user (unique email constraint)
- Authenticate credential (delegated partly to Identity provider later—in Week1 keep local bcrypt)
- Profile fetch/update minimal fields
- Optional: issue JWT after login (thin vs full IAM platform decision)

Quality attributes:

| Attribute | Concrete target illustration |
|-----------|-----------------------------|
| Availability | graceful degradation listing profile from cache staleness tolerated? |
| Scalability | stateless pods horizontal scale reads |
| Consistency | **strong** uniqueness on `(normalized_email)` at DB uniqueness |
| Security | bcrypt, TLS terminated at edge, audited admin operations |

Pick numbers only if justified (example): **Peak 200 reg/sec bursts** ⇒ shape pool sizes & DB concurrency.

---

## 2. High-level components

```text
[Browser/Mobile Clients]
       |
       v
[API Gateway]  -- TLS termination, rate limiting coarse, WAF hook optional
       |
       v
[User Service pods] --- (JWT signing keys / internal service auth)
       |
       v
[(Primary Postgres)]   hot path writes + reads authoritative profile
```

Add optional caches **only once** justification exists:

```
[Redis cache] <-- short TTL GET /users/{id} after write-through invalidations
```

**Rule:** caches complicate eviction—defer until measured hot read path (>100:1 skew maybe).

---

## 3. Data ownership sketch

Tables (align DB doc):

- **`users`** identity + bcrypt hash + versioning
- maybe **`sessions`** Week1 optional if JWT stateless simpler

Ownership: **single service touches `users`** (no peer microservice issuing direct SQL cross-BC).

Cross-service **`user_created` event optional** asynchronously for analytics—Week1 nice-to-have Kafka outline only.

---

## 4. Request flows

### Registration POST `/users`

Sequence (textual pseudo):

```text
Client -> Gateway(auth maybe open) -> UserService
validate DTO (@Valid)
normalize email lowercase
attempt INSERT
on unique violation -> 409
else -> 201 + Location header
audit log SUCCESS (MDC traceId correlation)
```

### Login POST `/sessions` conceptual

Credential verify:

```java
boolean ok = passwordEncoder.matches(provided, persistedHash);
```

On success issue signed JWT (**kid header** rotates keys advanced weeks).

Failures:

- mismatch → **`401 Unauthorized`** ambiguous message (prevent enumeration nuanced policy)

---

## 5. Scaling story

Horizontal:

- Stateless service pods ⇒ load balancer distributes
- Postgres vertical scale first; replicas for read offload later ⇒ replication lag caveat

Sharding users **premature** until tens+ millions realistically—focus connection pool correctness first.

---

## 6. Failure modes & mitigations

| Failure | Handling |
|---------|----------|
| DB outage | Bubble `503` + circuit breaker at gateway retries **bounded** reads only |
| Email provider async welcome optional down | degrade: still register sync; queue email event |
| Hot key email duplicate storms | DB constraint single source of truth |

---

## 7. Drawing checklist (Excalidraw)

Minimum shapes:

1. Client
2. Gateway (security boundary)
3. Service cluster
4. Primary DB
5. Optional cache
6. Async bus if events

Annotate arrows with **sync vs async** + **timeout** values.

---

## 8. Interview articulation script (30 seconds)

"We model `users` as an aggregate with uniqueness enforced at the database. The API gateway terminates TLS and enforces coarse rate limiting. Services are stateless to scale horizontally; we avoid caches until read amplification justifies eventual consistency complexity. Writes go to primary Postgres with Flyway-managed schema; failures map to standardized error codes for clients."
