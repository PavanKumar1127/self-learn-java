package com.selflearn.labs.networkfoundations;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates token-bucket rate limiting under burst traffic.
 */
public final class TokenBucketRateLimiterLab {

    private TokenBucketRateLimiterLab() {
    }

    public static void main(String[] args) {
        System.out.println("--- Token bucket burst simulation ---");
        TokenBucket limiter = new TokenBucket(100, 50.0); // capacity 100, refill 50 tokens/sec

        long nowMs = 0;
        long admitted = 0;
        long rejected = 0;

        // 1 second calm traffic: 20 rps
        for (int i = 0; i < 20; i++) {
            if (limiter.allow(nowMs += 50)) {
                admitted++;
            } else {
                rejected++;
            }
        }

        // Burst: 300 requests over 1 second
        for (int i = 0; i < 300; i++) {
            if (limiter.allow(nowMs += 3)) {
                admitted++;
            } else {
                rejected++;
            }
        }

        // Recovery: 2 seconds calm
        for (int i = 0; i < 40; i++) {
            if (limiter.allow(nowMs += 50)) {
                admitted++;
            } else {
                rejected++;
            }
        }

        System.out.println("capacity=100, refill=50 tokens/sec");
        System.out.println("admitted=" + admitted + ", rejected=" + rejected);
        System.out.println("finalEstimatedTokens=" + String.format("%.2f", limiter.tokens()));
        System.out.println("Takeaway: short burst is partially absorbed; sustained burst is throttled.");
    }

    static final class TokenBucket {
        private final long capacity;
        private final double refillPerMs;
        private double tokens;
        private final AtomicLong lastRefillMs;

        TokenBucket(long capacity, double refillPerSecond) {
            this.capacity = capacity;
            this.refillPerMs = refillPerSecond / 1000.0;
            this.tokens = capacity;
            this.lastRefillMs = new AtomicLong(0);
        }

        synchronized boolean allow(long nowMs) {
            refill(nowMs);
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        synchronized double tokens() {
            return tokens;
        }

        private void refill(long nowMs) {
            long last = lastRefillMs.get();
            long elapsed = Math.max(0, nowMs - last);
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed * refillPerMs));
                lastRefillMs.set(nowMs);
            }
        }
    }
}
