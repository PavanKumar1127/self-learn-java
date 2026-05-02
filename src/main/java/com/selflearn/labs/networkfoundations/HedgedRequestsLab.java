package com.selflearn.labs.networkfoundations;

import java.util.Arrays;
import java.util.Random;

/**
 * Simulates client-side hedged requests and shows latency vs amplification trade-off.
 */
public final class HedgedRequestsLab {

    private static final int REQUESTS = 20_000;

    private HedgedRequestsLab() {
    }

    public static void main(String[] args) {
        System.out.println("--- Hedged requests simulation ---");
        Random random = new Random(7);
        Simulation baseline = run(false, 0, random);
        random = new Random(7);
        Simulation hedged = run(true, 120, random); // launch hedge at 120ms

        print("baseline", baseline);
        print("hedged(120ms)", hedged);
    }

    private static Simulation run(boolean hedged, int hedgeDelayMs, Random random) {
        int[] latencies = new int[REQUESTS];
        long totalIssued = 0;
        for (int i = 0; i < REQUESTS; i++) {
            int primary = sampleLatency(random);
            int latency;
            if (!hedged || primary <= hedgeDelayMs) {
                latency = primary;
                totalIssued += 1;
            } else {
                int secondary = sampleLatency(random);
                latency = Math.min(primary, hedgeDelayMs + secondary);
                totalIssued += 2;
            }
            latencies[i] = latency;
        }
        Arrays.sort(latencies);
        return new Simulation(
                percentile(latencies, 50),
                percentile(latencies, 95),
                percentile(latencies, 99),
                totalIssued
        );
    }

    /**
     * Distribution with long tail:
     * - 90% around 40-80ms
     * - 9% around 120-220ms
     * - 1% around 450-850ms
     */
    private static int sampleLatency(Random random) {
        double r = random.nextDouble();
        if (r < 0.90) {
            return 40 + random.nextInt(41);
        }
        if (r < 0.99) {
            return 120 + random.nextInt(101);
        }
        return 450 + random.nextInt(401);
    }

    private static int percentile(int[] values, int p) {
        int idx = (int) Math.ceil((p / 100.0) * values.length) - 1;
        idx = Math.max(0, Math.min(values.length - 1, idx));
        return values[idx];
    }

    private static void print(String label, Simulation s) {
        double amp = (double) s.totalIssuedRequests / REQUESTS;
        System.out.println();
        System.out.println(label);
        System.out.println("  p50/p95/p99 ms = " + s.p50 + "/" + s.p95 + "/" + s.p99);
        System.out.println("  issued requests = " + s.totalIssuedRequests + " (" + String.format("%.3f", amp) + "x)");
    }

    private record Simulation(int p50, int p95, int p99, long totalIssuedRequests) {
    }
}
