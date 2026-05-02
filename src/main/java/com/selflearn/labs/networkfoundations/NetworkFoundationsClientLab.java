package com.selflearn.labs.networkfoundations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Exercises client-side networking behavior: status handling, timeouts, keep-alive reuse, and retries.
 */
public final class NetworkFoundationsClientLab {

    private final HttpClient client;
    private final String baseUrl;

    public NetworkFoundationsClientLab(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(700))
                .build();
    }

    public void runAll() throws Exception {
        System.out.println("--- Network foundations client lab ---");
        healthWarmup();
        postEcho();
        statusSemantics();
        timeoutDemo();
        flakyRetryDemo();
    }

    private void healthWarmup() throws IOException, InterruptedException {
        System.out.println("\n1) Keep-alive observation via repeated GET /health");
        for (int i = 1; i <= 5; i++) {
            long t0 = System.nanoTime();
            HttpResponse<String> res = send("GET", "/health", null, Duration.ofSeconds(2));
            long ms = (System.nanoTime() - t0) / 1_000_000;
            System.out.printf("   call=%d status=%d latencyMs=%d%n", i, res.statusCode(), ms);
        }
    }

    private void postEcho() throws IOException, InterruptedException {
        System.out.println("\n2) POST body + headers");
        String json = "{\"userId\":\"u-101\",\"action\":\"CREATE\"}";
        HttpResponse<String> res = send(
                "POST",
                "/echo",
                json,
                Duration.ofSeconds(2)
        );
        System.out.println("   status=" + res.statusCode());
        System.out.println("   body=" + res.body());
    }

    private void statusSemantics() throws IOException, InterruptedException {
        System.out.println("\n3) Status code semantics");
        HttpResponse<String> ok = send("GET", "/status/200", null, Duration.ofSeconds(1));
        HttpResponse<String> notFound = send("GET", "/status/404", null, Duration.ofSeconds(1));
        HttpResponse<String> serverErr = send("GET", "/status/503", null, Duration.ofSeconds(1));
        System.out.printf("   /status/200 -> %d%n", ok.statusCode());
        System.out.printf("   /status/404 -> %d (usually caller-side issue)%n", notFound.statusCode());
        System.out.printf("   /status/503 -> %d (often transient upstream issue)%n", serverErr.statusCode());
    }

    private void timeoutDemo() throws IOException, InterruptedException {
        System.out.println("\n4) Timeout demo: /slow?delayMs=1500 with 500ms request timeout");
        try {
            send("GET", "/slow?delayMs=1500", null, Duration.ofMillis(500));
            System.out.println("   unexpected: request succeeded");
        } catch (HttpTimeoutException e) {
            System.out.println("   timeout triggered as expected: " + e.getClass().getSimpleName());
        }
    }

    private void flakyRetryDemo() throws IOException, InterruptedException {
        System.out.println("\n5) Flaky upstream demo with bounded retries");
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpResponse<String> res = send("GET", "/flaky?failureRate=0.5", null, Duration.ofSeconds(1));
            System.out.printf("   attempt=%d status=%d body=%s%n", attempt, res.statusCode(), res.body());
            if (res.statusCode() < 500) {
                System.out.println("   success without exhausting retries");
                return;
            }
        }
        System.out.println("   all attempts failed -> this is where circuit-breaker/backoff policy matters.");
    }

    private HttpResponse<String> send(String method, String path, String body, Duration requestTimeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(requestTimeout)
                .header("Accept", "application/json")
                .header("X-Request-Id", "rf-client-1");

        if ("POST".equals(method)) {
            builder.header("Content-Type", "application/json");
            builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        } else {
            builder.GET();
        }

        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "http://localhost:8080";
        new NetworkFoundationsClientLab(base).runAll();
    }
}
