# AI for engineers — ~15 minutes/day literacy track

Goal: stay **effective** with AI tooling (Cursor, Claude, Copilot-class assistants) **without** replacing fundamentals. Use alongside `ROADMAP_90_DAY.md`.

**Daily micro-habit:** pick **one row** under the current week; do the **Practice** item in your repo or notebook.

---

## Week 1 — Prompting & context hygiene

| Day | Topic | Why it matters |
|-----|-------|----------------|
| 1 | **Task decomposition** — goal, constraints, “done”, non-goals in 5 lines | Reduces hallucinated scope |
| 2 | **Repo-grounded edits** — “only change files X/Y; preserve style” | Safer AI refactors |
| 3 | **Diff discipline** — require unified diffs / small commits | Easier rollback & review |
| 4 | **Test-first prompts** — “add failing test describing bug” before fix | Anchors correctness |
| 5 | **Negative constraints** — “no new deps; Java 25; Gradle only” | Stops dependency sprawl |

**Practice:** One AI-assisted refactor **with tests green** before merge.

---

## Week 2 — Code review partner mode

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | Risk triage — security vs perf vs correctness | Paste PR; ask for **ordered** findings |
| 2 | Complexity callout — big-O story for hot path | Force explicit complexity claim |
| 3 | Naming & boundaries — packages, DTO leakage | Rename pass via AI, you approve line-by-line |
| 4 | Error model — checked vs unchecked, domain exceptions | Generate exception map table |
| 5 | Mutation vs immutability in DTOs | Review immutable record usage |

---

## Week 3 — Security & hallucination traps

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Never paste secrets** — env vars + secret managers | Rotate any leaked test keys |
| 2 | JWT/prompt leakage — scrub tokens from logs/chats | Grep `.env` hygiene |
| 3 | Dependency suggestions — verify on Maven Central / official docs | Reject blindly added libs |
| 4 | SSRF/path traversal in AI-generated integrations | Threat-model one endpoint |
| 5 | PII in prompts — anonymize payloads | Strip real user emails from chat |

---

## Week 4 — Microservices prompts

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Idempotency-Key** in AI-generated clients | Sketch header + handler |
| 2 | Timeouts default audit | AI lists missing timeouts → you patch |
| 3 | Retry policy table | Compare naive vs backoff+jitter (link to labs) |
| 4 | Contract-first vs code-first APIs | Stub OpenAPI snippet |
| 5 | Cascading failure narrative | Write 5-bullet failure story |

---

## Week 5 — Messaging / events + AI

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **At-least-once** — dedup keys | Design idempotent consumer checklist |
| 2 | Poison messages — DLQ rationale | Outline DLQ + replay policy |
| 3 | Schema evolution — Avro/JSON | “additive only” migration note |
| 4 | Saga vs outbox wording | Compare in a table |
| 5 | Event naming — verbs vs facts | Rewrite 5 event names |

---

## Week 6 — Performance & eval

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Benchmark skepticism** — JVM warmup, variance | AI suggests JMH pitfalls list |
| 2 | Redis prompt patterns — TTL, stampede | One cache policy sentence |
| 3 | Rate limit design prompts | Tie to hash sharding / token bucket |
| 4 | GC/log noise from AI-added logging | Sampling / structured logs |
| 5 | Capacity “Fermi estimate” drill | Estimate QPS from story |

---

## Week 7 — Observability & reliability

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Structured logs** — correlation IDs | Align with `traceparent` lab themes |
| 2 | Metrics — RED/USE skim | Define 4 golden signals for one service |
| 3 | Traces — propagation across Gateway | Written sequence diagram |
| 4 | SLO/error budget wording | Pick one latency SLO |
| 5 | Postmortem template | Fill for a hypothetical outage |

---

## Week 8 — Containers, Dockerfiles, infra-as-text

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Dockerfile critique** — layers, distroless vs slim images | Paste Dockerfile; AI lists issues; you fix |
| 2 | **compose.yml** sanity — profiles, secrets *not* in git | Validate with `compose config` |
| 3 | **Healthchecks** aligned with JVM boot | Tune start_period |
| 4 | **SBOM / image scan awareness** — Trivy/Grype class tools | Interpret one CVE severity |
| 5 | **Human-in-the-loop** for infra PRs — no blind apply | List 5 “needs human gate” edits |

---

## Week 9 — Kubernetes YAML + GPT failure modes

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Requests/limits** — compare AI suggestion vs kube docs | One deployment patch |
| 2 | Probe tuning — readiness vs liveness | Write rationale in one paragraph |
| 3 | ConfigMap vs Secret — what must never hit CM | Classification table |
| 4 | **Hallucinated API versions** — always `kubectl explain` | Verify one resource field |
| 5 | **NetworkPolicy sketch** — least privilege egress | Pseudoyaml reviewed by you |

---

## Week 10 — Secrets Vault / KMS + AI

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Vault path layout** naming convention | Sketch tree for two services |
| 2 | **Never paste unwrap tokens / root tokens** into chat | Redact sample log line |
| 3 | KMS encryption context — ties ciphertext to tenant | Pseudocode encrypt/decrypt boundary |
| 4 | Rotate secret **runbook draft** via AI outline | You verify steps against docs |
| 5 | Dynamic DB cred vs static — trade-off bullets | Tie to JDBC URL reload story |

---

## Week 11 — Teleport / access plane + audit

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Teleport vs VPN** narrative (conceptual) | When each is inappropriate |
| 2 | **Session recording vs privacy** tension | Org policy bullets |
| 3 | SSO link to kubectl access | Draw identity flow |
| 4 | Audit log fields you’d alert on | 3 alerts max |
| 5 | **Break-glass** access story | Written 6-step drill |

*(If you don’t use Teleport: substitute “enterprise access gateway + audited kubectl”.)*

---

## Week 12 — Database design prompts + verification

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | AI-generated **migration** review — FKs & indexes | `EXPLAIN` one query |
| 2 | Normalize vs denorm — AI proposes; you defend | Money / inventory example |
| 3 | Pagination + cursor stability | Compare OFFSET vs cursor |
| 4 | Nullable vs sentinel values | Pick one invariant |
| 5 | Partitioning candidate — shard key pitfalls | Write “hot shard” paragraph |

---

## Week 13 — System design AI partner (disciplined)

| Day | Topic | Practice |
|-----|-------|-----------|
| 1 | **Capacity estimate cross-check** — AI Fermi vs your math | Reconcile divergence |
| 2 | Bottleneck critique on diagram — ordered list | Top 3 only |
| 3 | Failure mode table — blast radius ranked | Mitigation column |
| 4 | CAP / consistency story **for one read path** | Eventual vs strong |
| 5 | Executive summary **5 bullets** — no jargon soup | Interview voice |

---

## Cross-cutting AI topics (know as an engineer)

| Topic | Minimal competence |
|-------|---------------------|
| **Context windows** | What fits vs what must stay in wiki/runbooks |
| **Tool use / MCP / agents** | When automation is reversible vs destructive |
| **RAG over internal docs** | Ground answers; staleness risks |
| **Evals & regressions** | Prompt/instruction drift in internal bots |
| **PII / PCI / HIPAA class data** | **Never** in prompts; masking discipline |
| **Cost & rate limits** | Token awareness for batch automation |
| **IP / licensing** | Generated code provenance awareness |
| **Supply chain** | AI-suggested deps need same review as human deps |

---

## Weekly “stay current” (optional +10 min)

- Rotate: **JDK / Spring Boot / Kubernetes release notes skim** · **Kafka** · **OpenTelemetry** · **OWASP** · **Terraform/Helm** (pick one pillar per week).
- **Do not** optimize for novelty; optimize for **one actionable** change per week.

## Anti-patterns (memorize)

| Anti-pattern | Why it fails |
|--------------|---------------|
| “AI wrote it, ship it” | No reviewer = no ownership |
| Pasting prod data into chat | Incident-class mistake |
| Unbounded autonomy (auto-run prod) | Irreversible blast radius |
| **AI kubectl apply** without `diff`/`dry-run` | Cluster-wide outages |
