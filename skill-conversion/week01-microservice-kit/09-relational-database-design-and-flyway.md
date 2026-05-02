# Relational Database Design, SQL & Flyway Migrations

**Week 1 outcome:** schema you can **defend in review** + **versioned migrations** applied identically from laptop → CI → prod.

---

## 1. DDL vs DML (operational mental model)

| Class | Examples | Typical owner |
|-------|----------|---------------|
| **DDL** | `CREATE TABLE`, `ALTER`, `CREATE INDEX` | Flyway migration V* scripts (reviewed like code) |
| **DML** | `INSERT`, `UPDATE`, `DELETE`, `SELECT` | Application / batch jobs |

**Rule:** production schema shape changes are **DDL migrations**, not toggling Hibernate `ddl-auto=update`.

---

## 2. Constraints enforce invariants closer to data

| Constraint | Purpose |
|------------|---------|
| `PRIMARY KEY` | stable row identity (often surrogate `UUID` or `BIGSERIAL`) |
| `FOREIGN KEY` | referential integrity (`ON DELETE RESTRICT` vs `CASCADE` explicit choice) |
| `UNIQUE` | business uniqueness (`normalized_email`) |
| `NOT NULL` | mandatory fields at persistence layer |
| `CHECK` | domain rule (e.g., `balance >= 0`)—use sparingly but powerfully |

**Real scenario — users & orders:**

```sql
CREATE TABLE app_user (
    id              UUID PRIMARY KEY,
    email_normalized TEXT NOT NULL, -- use UNIQUE INDEX on lower(email_normalized); CITEXT optional Postgres extension
    password_hash   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    version         BIGINT NOT NULL DEFAULT 0 -- optimistic locking column
);

CREATE TABLE customer_order (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    status      TEXT NOT NULL,
    total_cents BIGINT NOT NULL CHECK (total_cents >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_order_user_created ON customer_order (user_id, created_at DESC);
```

**Why `ON DELETE RESTRICT` default mindset:** prevents accidental orphan cascades destroying financial history—choose `CASCADE` only with explicit product requirement.

---

## 3. Normalization vs pragmatic denormalization

| Normalized | Denormalized |
|------------|--------------|
| Less redundancy | Faster reads, more write complexity |
| Joins to assemble views | Risk inconsistent mirrors if updates not transactional |

Week 1: **normalize core OLTP paths** — denormalize only with measured read pain + invalidation strategy.

---

## 4. Indexes — cost/benefit

**Pros:** accelerate selective filters + range scans + FK join performance.

**Cons:** slower writes; extra disk; **wrong index** unused bloat.

Practical workflow:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM customer_order WHERE user_id = $1 ORDER BY created_at DESC LIMIT 20;
```

Watch for **`Seq Scan`** on large hot tables—justify with **`Index Scan`** / **`Bitmap`**.

Composite index column order follows **equality filters first**, then sort/range columns.

---

## 5. Transactions (ACID) & isolation (Spring alignment)

Declare at **service** orchestration boundaries:

```java
@Service
public class OrderService {

    @Transactional
    public OrderResponse place(PlaceOrderCommand cmd) {
        // multiple repository calls commit atomically (single transaction)
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> listForUser(UUID userId, Pageable page) {
        return orders.findByUserId(userId, page);
    }
}
```

**Principle:** keep transactions **short**—no external HTTP inside unless using **outbox / async** patterns (later weeks).

---

## 6. Flyway layout

```
src/main/resources/db/migration
  V1__create_app_user.sql
  V2__create_customer_order.sql
  V3__add_user_locale.sql
```

**Versioning rules:**

- **Never** edit applied migration in shared environments (append new `V4__...` instead).
- **Expand-contract** pattern for zero-downtime later: add column nullable → backfill → enforce NOT NULL in later release.

---

## 7. Example Flyway script

```sql
-- V1__create_app_user.sql
CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email_normalized TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX ux_app_user_email_norm ON app_user (lower(email_normalized));
```

### Rollbacks

Flyway community edition: **write forward fix** migrations rather than automatic down (`U` scripts) unless using paid features / custom discipline.

---

## 8. JPA mapping alignment (brief)

```java
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    private UUID id;

    @Column(name = "email_normalized", nullable = false, length = 320)
    private String emailNormalized;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Version
    private long version;

    // getters / behavior methods
}
```

**Best practice:** entity reflects migration truth—**don’t** let Hibernate silently drift schema in prod.

---

## 9. Common Week-1 schema mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Missing uniqueness on email | duplicate accounts | DB unique index (not only app check) |
| `BIGINT` money | rounding errors | store **minor units** (`cents`) as integer |
| Overusing `TEXT` without length in API validation | abuse payloads | validation + DB length guard |
| Nullable everything | ambiguous business state | intentional nullability |
