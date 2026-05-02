# 90-day skill conversion roadmap (~13 weeks, 2 hr/day)

**Split:** ~**60 min DSA** · ~**60 min Backend + platform + system design** (rotate emphasis; not every topic every day).

**Throughput reality:** ~**90 sessions** stays interview-ready only if you **ship artifacts**: code, migrations, manifests, diagrams, or runbook bullets.

---

## How the backend hour rotates (practical)

| Day type | Focus |
|----------|--------|
| **Mon–Tue** | Application code (Spring, JPA, APIs) |
| **Wed** | **Database design** or query/transaction depth |
| **Thu** | **Containers / K8s / platform** lab (small, repeatable) |
| **Fri** | **System design** sketch (30–45 min) + retro / mistakes log |

If a week is heavy (e.g. Kafka), shift **Thu platform** to **typed notes + one diagram** instead of a full cluster lab.

---

## Master map: 13 weeks × dimensions

**90 days ≈ 13 weeks.** Use this as the single source of truth; week detail sections below expand it.

| Wk | Application / Spring | **Containers & runtime** | **Data & DB design** | **Security, secrets, encryption** | **System design (HLD cadence)** | DSA spine |
|----|----------------------|---------------------------|------------------------|-----------------------------------|----------------------------------|-----------|
| **1** | C–S–R, validation, exceptions | **Dockerfile** for one service (multi-stage, non-root, health) | Table design for one aggregate; PK/FK; **migration** (Flyway/Liquibase) | TLS to client at edge (concept); no secrets in Git | **SD-1:** API + data model for one resource (draw box diagram) | Arrays, hashing, prefix sums |
| **2** | JPA tuning, transactions, N+1 | **`docker compose`** run app + DB + wire env | **Normalization vs denorm**; **indexes** (B-tree mental model); `EXPLAIN` story | DB creds via env / secret mount (not source) | **SD-2:** Read-heavy list endpoint — caches optional | Sliding window, heaps |
| **3** | JWT, RBAC, gateway auth | Compose add **gateway** container or prod-like network | Nullable columns, uniqueness, soft-delete trade-offs | **Vault** basics: static secrets, policies, KV path layout; rotate story | **SD-3:** AuthN/Z flows across Gateway + service | Graphs introduce |
| **4** | Microservices cuts, timeouts/retries | **Compose**: 2 services + DB + kafka/redis optional | Schema versioning; **backward-compatible** DDL | **mTLS**/service mesh (concept); cert rotation awareness | **SD-4:** Gateway + services + timeouts on diagram | Trees, Trie |
| **5** | Kafka consumers, idempotency | Containers for broker + app (or managed mock) | **Outbox**/dedup tables; append-only logs | KMS-at-rest skim; encrypt **PII column** vs disk encryption | **SD-5:** Event-driven notify / saga sketch | DFS/backtracking select |
| **6** | Caching, rate limiting | **K8s-1:** Pod, Deployment, Service, **readiness/liveness** | Read replicas vs cache; staleness SLA | Secrets: **K8s Secret vs Vault** (trade-off table below) | **SD-6:** Rate limit + cache layer on prior design | DP patterns subset |
| **7** | Observability (logs/metrics/traces) | **K8s-2:** ConfigMap, resource limits, **HPA skim** | Hot keys, partitioning idea; slow query playbook | Audit logs; **who accessed prod** narrative | **SD-7:** Cross-service tracing story | Mixed timed review |
| **8** | Capstone wiring | **K8s-3:** Ingress (or NodePort/minikube path); rolling update | Consolidated **ER diagram** + index plan for capstone | **Teleport** (or similar): **privileged access**, kubectl/SSH audit, SSO | **SD-8:** Full capstone HLD (see checklist) | Weakest-pattern sprint |
| **9** | Hardening capstone | **K8s-4:** Namespaces, NetworkPolicy (basics), RBAC skim | Backup/restore story; RPO/RTO one-pager | **Vault** dynamic DB creds *or* deep policy exercise | **SD-9:** Failure modes + blast radius | Mock interview block |
| **10** | Load & chaos mindset | **K8s-5:** Helm or Kustomize *or* one GitOps concept | Connection pool sizing vs DB max connections | Encryption in transit everywhere; HSTS / modern TLS | **SD-10:** Capacity estimate + bottleneck | Mock interview block |
| **11** | Polish APIs & docs | Image signing / SBOM *awareness* (scan one image) | Index review; remove accidental table scans | Secret rotation runbook | **SD-11:** Deep dive one hard component (payments/ledger) | Review + weak cards |
| **12** | Cost & operability | Node affinity/taint *awareness*; pod disruption budgets skim | Archival / TTL for data | Break-glass access procedure | **SD-12:** Multi-region *paper* design | Timed sets |
| **13** | Interview lane | One full env from scratch (**compose or k8s**) in <2h checklist | Explain any table on whiteboard | Red-team yourself: leaked token + recovery | **SD-13:** Presentation run (10–15 min) | Full mixed review |

---

## Trade-off tables (production)

### Vault vs Kubernetes Secret vs env-in-CI

| Approach | Strength | Risk |
|-----------|----------|------|
| **Plain env / flat files** | Fast local dev | Leak via logs, snapshots, chats |
| **K8s Secret** | Native to cluster | etcd encryption at rest varies; RBAC-sensitive; rotation manual |
| **Vault (or cloud secret manager)** | Policies, audit, rotation, dynamic creds | Ops complexity; availability becomes critical path |
| **Teleport (access plane)** | Central **audit**, SSO, session control for humans to infra | Not a substitute for Vault; orthogonal (who can reach kube/SSH) |

**Principle:** **Secrets never in Git**; **humans rarely use long-lived shared kubeconfigs** without audit.

### Encryption (what “encryption” usually means here)

| Layer | Typical tool | Engineer responsibility |
|-------|----------------|---------------------------|
| **In transit** | TLS (terminating at LB/gateway/service mesh) | Correct certs, min TLS version, SNI/host rules |
| **At rest (disk)** | DB/cloud volume encryption | Enable by default on managed DB |
| **At rest (column / field)** | App crypto + KMS | Key rotation, searchable-encryption trade-offs |
| **In backups** | Same as volume + access control | Restore drills |

---

## Week 1 — Application + first container

**Add vs old plan:** **Dockerfile** + **one Flyway migration** + **SD-1** diagram.

- Image: non-root user, `JAVA_OPTS`, **healthcheck** aligned with Spring Actuator if you add it.
- DB: one table with explicit constraints in migration (not only JPA auto-DDL for prod story).

---

## Week 2 — Data layer + compose

**Add:** **Database design** sprint + **docker compose** (app + postgres).

DB design drills:

- Identify **candidate keys**, **foreign keys**, **ON DELETE** behavior.
- Decide **natural vs surrogate** PK for one entity.
- Add **indexes** justified by query (not speculative everywhere).
- Read one **`EXPLAIN / ANALYZE`** plan (PostgreSQL-style).

---

## Week 3 — Security + Vault + encryption narrative

**Add:** Introduce **HashiCorp Vault** (dev server acceptable) **or** your cloud vendor secret manager analog.

Labs:

- Mount path convention (`secret/data/order-service/dev/...`).
- Read secret from app at startup (**not** baked in image).

**Teleport (conceptual/production):**

- Humans access **clusters/SSH** through identity-aware proxy.
- **Audit trail** for compliance; short-lived certs vs static keys.
- Map to your org: “How would on-call `kubectl` without shared `kubeconfig` on laptops?”

**Encryption:** one-page note: TLS termination, at-rest DB, when app-level encryption is worth it.

---

## Week 4 — Microservices + multi-container dev

**Add:** **docker compose** with **two services** + shared network; explicit **depends_on** + health ordering where needed.

SD: draw **Gateway → service A/B → DBs** with timeout and retry annotations.

---

## Week 5 — Messaging + event DB patterns

**Add:** Event **outbox** table or idempotency table design; containerized broker optional.

---

## Week 6 — Scale + **Kubernetes I**

**K8s minimum competent set:**

- Pod, Deployment, Service (ClusterIP), **liveness/readiness**
- **ConfigMap** for non-secret config
- **Resource requests/limits** (avoid noisy neighbor)

SD: add **cache + rate limit** to prior design.

---

## Week 7 — Reliability + **Kubernetes II**

**Add:** HPA concept (CPU or custom metric story); **PodDisruptionBudget** awareness.

Observability ties to K8s: scrape targets, sidecar vs daemonset (conceptual).

---

## Week 8 — Capstone + **Kubernetes III** + **Teleport**

**Capstone checklist (HLD):**

| Area | Boxes you must draw |
|------|---------------------|
| Edge | CDN? WAF? **TLS** termination |
| Gateway | Auth, routing, rate limit |
| Core services | Owned data per service |
| Data | OLTP stores, caches, indexes |
| Async | Bus + **idempotency** + DLQ |
| Platform | **K8s** deploy units; config/secrets sources |
| Security | Vault; **Teleport**/SSO for human access |
| Observability | Logs/metrics/traces + SLO sketch |

---

## Weeks 9–13 — Consolidation lane

Purpose: convert breadth into **credible Senior SDE narrative**.

- Repeat **deploy from scratch** exercise weekly (compose or single-node k8s).
- Rotate: **failure injection** story, **capacity estimate**, **index review**, **runbook**.
- Maintain **Production Mistakes Log** with one “near miss” per week minimum.

---

## System design recurring drills (themes)

Alternate weekly **focus** so you aren’t repetitive:

| Week focus | Example prompts |
|------------|-------------------|
| **Core web** | URL shortener, paste bin, leaderboard |
| **Social / feed** | Twitter-lite, notifications |
| **Commerce** | Inventory reservation, checkout, idempotency |
| **Realtime** | Chat typing indicators (conceptual WS) |

Use **estimated QPS**, **latency SLO**, and **failure** column on each diagram.

---

## AI literacy (paired with roadmap)

Dedicated track: **`AI_DAILY_TRACK.md`** (extended through week 13).

**Weekly principle:** use AI for **drafting** migrations, Dockerfile, kubectl YAML — **always** verify against docs and dry-run locally.

---

## Links inside this repo

- HTTP / retries / backoff / traces: `src/main/java/com/selflearn/labs/networkfoundations/`
- Hashing internals (rate limiting intuition): `src/main/java/com/selflearn/labs/hashmap/`
