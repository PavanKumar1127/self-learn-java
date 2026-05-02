# Foundation First (1-2 weeks): Internet + HTTP for Backend Engineers

Audience: software engineers who already write APIs and want practical, production-relevant depth.

Companion classes in this package:
- `NetworkFoundationsServer`
- `NetworkFoundationsClientLab`
- `NetworkFoundationsEmbeddedDemo`

Quick start:
1. `./gradlew runNetworkFoundationsDemo`
2. `./gradlew runNetworkFoundationsServer`
3. In another terminal: `./gradlew runNetworkFoundationsClient`
4. Run curl/Postman exercises in this file.

---

## 1) How an internet request actually moves

For `https://api.partner.com/orders` from your service:

1. Your app checks local DNS cache, then resolver cache, then authoritative DNS if needed.
2. Resolver returns one or more IPs (A/AAAA), each with TTL.
3. Client opens TCP connection to target IP:port (`443` for HTTPS).
4. TLS handshake happens (cert validation, key agreement, session resumption if available).
5. HTTP request bytes are sent over TCP stream.
6. Server (or LB/proxy chain) processes request and sends HTTP response.
7. Connection is either kept alive (pooled) or closed.

Practical latency budget:
- DNS lookup: 1-30 ms if cache miss.
- TCP + TLS handshake: 1-3 RTTs.
- Request/response transfer + server time: dominant part under load.

Key production insight:
- A single high-level "HTTP call" can include multiple hidden retries and multiple network round trips.

---

## 2) DNS resolution (`api.partner.com -> IP`)

What matters for engineers:
- **TTL behavior**: very low TTL can increase resolver load and variance.
- **Multiple A/AAAA records**: clients may pick different IPs; this impacts canary and failover.
- **Negative caching**: NXDOMAIN can be cached and delay recovery after DNS mistakes.
- **Stale DNS in pools**: a long-lived connection pool may keep talking to old backends even after DNS rotation.

Failure modes:
- Resolver outage or high latency.
- Split-horizon DNS mismatch between environments.
- IPv6 preferred path broken while IPv4 works (or vice versa).

What to observe:
- `time_namelookup` from curl timing.
- Actual remote IP used (`-v` in curl).

---

## 3) TCP basics (SYN / SYN-ACK / ACK)

Connection setup:
- Client sends `SYN`.
- Server replies `SYN-ACK`.
- Client replies `ACK`.
- Data can flow.

Why this matters:
- New connection setup costs RTT and kernel resources.
- Under bursts, SYN backlog/accept queue pressure can cause connection failures/timeouts.

Packet loss and retransmission:
- TCP guarantees ordered byte stream by retransmitting lost segments.
- Retransmission avoids data loss but adds tail latency.
- Bursty loss triggers multiple retransmission timeouts and throughput collapse.

Engineering implications:
- Reuse connections (keep-alive, pooling) to avoid handshake tax.
- Set explicit connect/read/request timeouts to avoid hung threads.
- Distinguish connect timeout vs response timeout in telemetry.

---

## 4) HTTP essentials

### Methods
- `GET`: safe/read-only semantics (still can be expensive server-side).
- `POST`: create/process command; usually non-idempotent unless you add idempotency keys.
- `PUT`: replace resource representation (often idempotent).
- `PATCH`: partial update.
- `DELETE`: remove resource (usually idempotent semantics expected).

### Headers/body
- Headers carry metadata: auth, tracing, content negotiation, caching.
- Body carries payload.
- `Content-Type` and `Accept` are contract-critical.

### Status codes and what they imply operationally
- `2xx`: success. Still inspect body for domain-level errors.
- `4xx`: caller-side issue (validation/auth/not found/rate-limited).
- `5xx`: server-side issue. Candidates for retry depend on idempotency and error type.

Retry heuristics:
- Prefer retry for transient `5xx`, `429`, connection resets, timeouts.
- Never blind-retry non-idempotent writes without idempotency strategy.

---

## 5) Keep-alive, pooling, and timeouts

Keep-alive/pooling:
- Reuses established TCP/TLS sessions.
- Reduces latency and CPU.
- Increases throughput and resilience under burst traffic.

Timeouts you must set:
- **Connect timeout**: how long to establish socket.
- **Request timeout**: total request wall-clock cap.
- **Read timeout** (or per-stage timeout): response wait cap.

Pool tuning basics:
- Max total connections and per-route limits.
- Idle eviction and max lifetime.
- Back-pressure when pool is exhausted.

Common anti-pattern:
- Infinite timeout + unbounded retries = cascading failure during partner outage.

---

## 6) JSON and serialization

What senior engineers watch:
- Schema evolution over time, not just parse success today.
- Nullability semantics (`missing` vs `null` vs empty).
- Forward/backward compatibility across independently deployed services.

Compatibility patterns:
- Additive fields are safest.
- Never rename/remove required fields without migration window.
- Use defaults for new optional fields.
- Treat unknown fields as ignorable when possible.

Schema mismatch examples:
- Producer sends enum value unknown to old consumer.
- Numeric/string type drift (`id: 123` -> `"123"`).
- Field becomes nullable but consumer assumes non-null.

Guardrails:
- Contract tests (consumer-driven or schema registry checks).
- Explicit versioning policy.
- Validation + clear `4xx` errors for contract violations.

---

## 7) Hands-on: curl/Postman drills

Start local server:

```bash
./gradlew runNetworkFoundationsServer
```

### Drill A: Methods, headers, body

```bash
curl -i http://localhost:8080/health
curl -i -X POST http://localhost:8080/echo -H "Content-Type: application/json" -H "X-Request-Id: rf-1" -d '{"name":"alice","active":true}'
curl -i http://localhost:8080/status/404
curl -i http://localhost:8080/status/500
```

What to inspect:
- Request/response headers (`Content-Type`, `Connection`, trace IDs).
- Status code semantics.
- Body shape consistency.

### Drill B: latency and timeout behavior

```bash
curl -i "http://localhost:8080/slow?delayMs=1500"
curl -s -o /dev/null -w "dns=%{time_namelookup} connect=%{time_connect} starttransfer=%{time_starttransfer} total=%{time_total}\n" http://localhost:8080/health
```

### Drill C: replay failures

```bash
for i in {1..10}; do curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/flaky?failureRate=0.4"; done
```

In Postman:
- Save failing responses.
- Replay same request with and without timeout changes.
- Add/remove headers to reproduce auth/content-type failures.

---

## 8) Weekly exercise plan (1-2 weeks)

### Week 1
1. Trace one outbound dependency call from your app logs:
   - DNS time, connect time, TLS, server time, total.
2. Implement strict timeout policy in one HTTP client.
3. Add idempotency key support for one write endpoint.
4. Build retry matrix by method/status/error.

### Week 2
1. Simulate 30% failure + 2s latency in local lab (`/flaky`, `/slow`).
2. Add circuit breaker/bulkhead (if your stack has it).
3. Write contract test for nullable + missing field behavior.
4. Produce a one-page runbook:
   - "Partner API high latency"
   - "Spike in 5xx/429"
   - "DNS resolution failures"

---

## 9) What engineers should know (practical checklist)

Detailed expansion: `PracticalChecklistDeepDive.md`.

- Difference between DNS, TCP connect, TLS handshake, request latency.
- How retries multiply load and can worsen incidents.
- Why keep-alive and pool sizing directly affect p99 latency.
- How to classify errors into retryable/non-retryable safely.
- Why idempotency is mandatory for resilient write APIs.
- How to evolve JSON schemas without breaking old clients.
- How to capture raw request/response and replay incidents.
- How to read timeout metrics and distinguish where time was spent.

---

## 10) Suggested next labs after this package

Implemented mapping and commands: `NextLabsGuide.md`.

- Add exponential backoff + jitter strategy and compare against fixed retry.
- Add a token-bucket rate limiter and test with burst traffic.
- Add client-side hedged requests and evaluate cost vs p99 win.
- Add simple OpenTelemetry trace propagation through local server.
