package com.selflearn.labs.networkfoundations;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Compares fixed-delay retries vs exponential backoff with jitter under transient outage.
 */
public final class RetryBackoffJitterLab {

    private static final int REQUESTS = 2_000;
    private static final int MAX_ATTEMPTS = 5;
    private static final int OUTAGE_MS = 250;
    private static final int SERVICE_TIME_MS = 20;

    private RetryBackoffJitterLab() {
    }

    public static void main(String[] args) {
        System.out.println("--- Retry strategy simulation: fixed vs backoff+jitter ---");
        SimulationResult fixed = runSimulation("fixed", new Random(42));
        SimulationResult jitter = runSimulation("exp-jitter", new Random(42));
        printResult(fixed);
        printResult(jitter);
    }

    private static SimulationResult runSimulation(String mode, Random random) {
        int[] completionMs = new int[REQUESTS];
        int success = 0;
        int attempts = 0;
        Map<Integer, Integer> retriesPer50ms = new TreeMap<>();

        for (int i = 0; i < REQUESTS; i++) {
            int nowMs = 0;
            boolean done = false;
            for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
                attempts++;
                if (attempt > 1) {
                    int bucket = (nowMs / 50) * 50;
                    retriesPer50ms.merge(bucket, 1, Integer::sum);
                }

                boolean serviceHealthy = nowMs >= OUTAGE_MS;
                if (serviceHealthy) {
                    nowMs += SERVICE_TIME_MS;
                    completionMs[i] = nowMs;
                    success++;
                    done = true;
                    break;
                }
                if (attempt < MAX_ATTEMPTS) {
                    nowMs += delay(mode, attempt, random);
                }
            }
            if (!done) {
                completionMs[i] = nowMs;
            }
        }

        Arrays.sort(completionMs);
        int p50 = percentile(completionMs, 50);
        int p95 = percentile(completionMs, 95);
        int p99 = percentile(completionMs, 99);
        int peak = retriesPer50ms.values().stream().max(Integer::compareTo).orElse(0);
        return new SimulationResult(mode, success, attempts, p50, p95, p99, peak);
    }

    private static int delay(String mode, int attempt, Random random) {
        if ("fixed".equals(mode)) {
            return 100;
        }
        int base = 100;
        int maxBackoff = base << (attempt - 1); // 100,200,400...
        int floor = maxBackoff / 2; // equal-jitter floor avoids synchronized immediate retries
        return floor + random.nextInt((maxBackoff - floor) + 1);
    }

    private static int percentile(int[] values, int p) {
        int idx = (int) Math.ceil((p / 100.0) * values.length) - 1;
        idx = Math.max(0, Math.min(values.length - 1, idx));
        return values[idx];
    }

    private static void printResult(SimulationResult r) {
        System.out.println();
        System.out.println("Strategy: " + r.strategy);
        System.out.println("  totalRequests      = " + REQUESTS);
        System.out.println("  successfulRequests = " + r.successful);
        System.out.println("  totalAttempts      = " + r.totalAttempts + " (" + String.format("%.2f", (double) r.totalAttempts / REQUESTS) + "x)");
        System.out.println("  completion p50/p95/p99 (ms) = " + r.p50Ms + "/" + r.p95Ms + "/" + r.p99Ms);
        System.out.println("  peak retries per 50ms bucket  = " + r.peakAttemptsPerBucket);
    }

    private record SimulationResult(
            String strategy,
            int successful,
            int totalAttempts,
            int p50Ms,
            int p95Ms,
            int p99Ms,
            int peakAttemptsPerBucket
    ) {
    }
}
