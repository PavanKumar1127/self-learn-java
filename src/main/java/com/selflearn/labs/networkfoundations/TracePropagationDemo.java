package com.selflearn.labs.networkfoundations;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.Executors;

/**
 * Minimal W3C traceparent propagation demo (A -> B) without adding external dependencies.
 */
public final class TracePropagationDemo {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TracePropagationDemo() {
    }

    public static void main(String[] args) throws Exception {
        HttpServer serviceB = startServiceB(8092);
        HttpServer serviceA = startServiceA(8091);
        try {
            System.out.println("Service A: http://localhost:8091");
            System.out.println("Service B: http://localhost:8092");
            callAWithoutTrace();
            callAWithTrace();
        } finally {
            serviceA.stop(0);
            serviceB.stop(0);
            System.out.println("Trace propagation demo stopped.");
        }
    }

    private static HttpServer startServiceA(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4, r -> daemonThread("trace-a", r)));
        server.createContext("/a", exchange -> {
            String incoming = header(exchange, "traceparent");
            TraceContext parent = TraceContext.fromOrCreate(incoming);
            TraceContext child = parent.child();

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(300)).build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8092/b"))
                    .timeout(Duration.ofSeconds(1))
                    .header("traceparent", child.format())
                    .GET()
                    .build();

            HttpResponse<String> bRes;
            try {
                bRes = client.send(req, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while calling service B", e);
            }
            String body = "{"
                    + "\"service\":\"A\","
                    + "\"incomingTraceparent\":\"" + escape(orDefault(incoming, "")) + "\","
                    + "\"outgoingTraceparent\":\"" + child.format() + "\","
                    + "\"downstream\":" + bRes.body()
                    + "}";
            sendJson(exchange, 200, body, child.format());
        });
        server.start();
        return server;
    }

    private static HttpServer startServiceB(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4, r -> daemonThread("trace-b", r)));
        server.createContext("/b", exchange -> {
            String incoming = header(exchange, "traceparent");
            TraceContext ctx = TraceContext.fromOrCreate(incoming);
            String body = "{"
                    + "\"service\":\"B\","
                    + "\"traceparent\":\"" + ctx.format() + "\""
                    + "}";
            sendJson(exchange, 200, body, ctx.format());
        });
        server.start();
        return server;
    }

    private static void callAWithoutTrace() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8091/a"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println();
        System.out.println("Call A without incoming traceparent:");
        System.out.println("  response traceparent header = " + res.headers().firstValue("traceparent").orElse(""));
        System.out.println("  body = " + res.body());
    }

    private static void callAWithTrace() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String traceparent = TraceContext.createRoot().format();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8091/a"))
                .timeout(Duration.ofSeconds(2))
                .header("traceparent", traceparent)
                .GET()
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println();
        System.out.println("Call A with incoming traceparent:");
        System.out.println("  sent traceparent = " + traceparent);
        System.out.println("  response traceparent header = " + res.headers().firstValue("traceparent").orElse(""));
        System.out.println("  body = " + res.body());
    }

    private static Thread daemonThread(String prefix, Runnable r) {
        Thread t = new Thread(r, prefix + "-" + System.nanoTime());
        t.setDaemon(true);
        return t;
    }

    private static String header(HttpExchange exchange, String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    private static void sendJson(HttpExchange exchange, int status, String body, String traceparent) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().add("traceparent", traceparent);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record TraceContext(String traceId, String spanId, String flags) {

        static TraceContext createRoot() {
            return new TraceContext(randomHex(16), randomHex(8), "01");
        }

        static TraceContext fromOrCreate(String traceparent) {
            if (traceparent == null || traceparent.isBlank()) {
                return createRoot();
            }
            String[] parts = traceparent.split("-");
            if (parts.length != 4) {
                return createRoot();
            }
            return new TraceContext(parts[1], parts[2], parts[3]);
        }

        TraceContext child() {
            return new TraceContext(traceId, randomHex(8), flags);
        }

        String format() {
            return "00-" + traceId + "-" + spanId + "-" + flags;
        }
    }

    private static String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        return HexFormat.of().formatHex(b);
    }
}
