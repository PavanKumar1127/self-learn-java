# Practical Checklist Deep Dive (Engineer Edition)

This expands section 9 from `FoundationFirst.md` into implementation-level guidance.

---

## 1) DNS vs TCP connect vs TLS handshake vs request latency

Treat these as separate latency components:
- **DNS**: name resolution (`api.partner.com -> IP`). Influenced by cache hit/miss and resolver health.
- **TCP connect**: socket establishment (`SYN/SYN-ACK/ACK`) to selected IP.
- **TLS handshake**: certificate exchange + key agreement before encrypted HTTP.
- **Request latency**: time from first request byte sent to response received.

Why separation matters:
- Slow DNS needs resolver/cache fixes, not HTTP retry tuning.
- Slow connect points to routing/firewall/LB saturation.
- Slow TLS points to cert/crypto/CPU/session resumption issues.
- Slow request latency points to app logic or downstream saturation.

Operational rule: emit metrics with these labels separately; never ship only one "http_client_total_ms".

---

## 2) How retries multiply load and worsen incidents

Basic multiplication:
- If each request does up to 3 retries, worst-case attempts become up to 4x original traffic.
- Under widespread transient failures, this can amplify pressure on an already unhealthy dependency.

Failure amplification loop:
1. dependency slows
2. callers timeout
3. callers retry immediately
4. dependency receives more load
5. latency worsens and failure fan-out increases

Controls:
- cap retries (usually 1-2 for synchronous user path)
- use exponential backoff + jitter
- gate retries to idempotent operations
- add circuit breaker to stop retry storms

See runnable lab: `RetryBackoffJitterLab`.

---

## 3) Why keep-alive and pool sizing affect p99

Without pooling:
- every call pays connect + TLS cost
- kernel/socket churn increases
- p99 tails rise during bursts

With pooling:
- warm reused connections collapse per-call setup overhead
- fewer handshakes means lower CPU and lower tail latency

Pool pitfalls:
- too small: queueing and head-of-line delay
- too large: connection thrash, memory pressure, stale connections
- no idle/max-lifetime eviction: stale endpoints and slow failures

Tune with production signals:
- pool acquire wait time
- connect timeout rate
- handshake failures
- in-flight per-route utilization

---

## 4) Classifying retryable vs non-retryable safely

Retryable (typically):
- connect timeout
- read timeout on idempotent operation
- `429` with backoff
- transient `5xx` (`502/503/504`)

Usually non-retryable:
- validation errors (`400/422`)
- auth errors (`401/403`)
- not found (`404`) unless eventual consistency flow explicitly expects it
- semantic conflict (`409`) unless caller has merge/reconcile logic

Key rule:
- classify by **error + operation semantics**. Same status can be retryable for one call and dangerous for another.

---

## 5) Why idempotency is mandatory for resilient writes

Write-path retries without idempotency can duplicate effects:
- duplicate payments
- duplicate order creation
- duplicate outbound messages

Pattern:
1. client sends `Idempotency-Key`
2. server stores key + canonical result
3. repeated request with same key returns prior result, not a second side effect

Scope decisions:
- key uniqueness window (e.g. 24h)
- payload hash validation per key
- storage model (cache/db)

---

## 6) Evolving JSON schema without breaking old clients

Compatibility-safe moves:
- add optional fields
- keep old fields during migration
- preserve enum values or add tolerant unknown handling

Breaking moves (avoid without versioning):
- remove/rename required fields
- type change (`number` -> `string`) without dual-read period
- changing nullability without compatibility contract

Practical guardrails:
- schema/contract tests in CI
- consumer-driven contract checks for major integrations
- explicit deprecation windows and migration runbook

---

## 7) Capturing raw request/response and replaying incidents

Capture:
- method, URL, query, headers (with sensitive redaction), body, status, response headers, timing
- correlation IDs (`X-Request-Id`, `traceparent`, etc.)

Replay:
- reproduce exact request with curl/Postman
- vary one dimension at a time (timeout, header, payload size)
- compare against known-good baseline

Outcome:
- faster root cause isolation, especially for partner API and intermittent failures.

---

## 8) Reading timeout metrics correctly

Track distinct counters/timers:
- DNS lookup timeout/failure
- connect timeout
- TLS handshake timeout/failure
- request timeout (overall)
- read timeout (response stage)

Diagnostic examples:
- connect timeout spike + normal server processing time => network path/LB issue
- read timeout spike + high server processing => dependency overload
- request timeout only + low connect => queueing or application bottleneck

---

## Companion labs for this deep dive

- `RetryBackoffJitterLab`
- `TokenBucketRateLimiterLab`
- `HedgedRequestsLab`
- `TracePropagationDemo`
