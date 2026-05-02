# Suggested Next Labs (Implemented)

This file maps each suggested lab from `FoundationFirst.md` to a runnable class.

---

## 1) Exponential backoff + jitter vs fixed retry

- Class: `RetryBackoffJitterLab`
- Run: `./gradlew runRetryBackoffJitterLab`
- What it shows:
  - request amplification under outage
  - peak retry concentration ("retry storm")
  - completion latency impact by strategy

Expected takeaway:
- fixed delay retries synchronize and create spikes; jitter spreads load and lowers peak pressure.

---

## 2) Token-bucket rate limiter under burst traffic

- Class: `TokenBucketRateLimiterLab`
- Run: `./gradlew runTokenBucketRateLimiterLab`
- What it shows:
  - admitted vs rejected requests during burst
  - recovery after burst
  - effect of bucket capacity/refill rate on protection vs throughput

Expected takeaway:
- token bucket absorbs short bursts but enforces steady-state rate to protect dependencies.

---

## 3) Client-side hedged requests and p99 trade-off

- Class: `HedgedRequestsLab`
- Run: `./gradlew runHedgedRequestsLab`
- What it shows:
  - baseline latency percentiles
  - latency percentiles with hedging after a threshold
  - extra request cost (amplification factor)

Expected takeaway:
- hedging can significantly improve p99, but increases backend load; only safe with budgeted amplification.

---

## 4) Simple OpenTelemetry-style trace propagation

- Class: `TracePropagationDemo`
- Run: `./gradlew runTracePropagationDemo`
- What it shows:
  - incoming/outgoing `traceparent` propagation across service A -> service B
  - child span creation with same trace ID
  - correlation of logs and response headers

Note:
- This demo uses W3C `traceparent` semantics without pulling an OTel SDK dependency, to keep fundamentals explicit.

---

## Recommended order

1. `runRetryBackoffJitterLab`
2. `runTokenBucketRateLimiterLab`
3. `runHedgedRequestsLab`
4. `runTracePropagationDemo`
