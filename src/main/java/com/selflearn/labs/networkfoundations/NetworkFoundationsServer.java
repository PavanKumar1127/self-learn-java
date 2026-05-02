package com.selflearn.labs.networkfoundations;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local HTTP server used by foundation exercises.
 *
 * Endpoints:
 * - GET  /health
 * - POST /echo
 * - GET  /status/{code}
 * - GET  /slow?delayMs=...
 * - GET  /flaky?failureRate=0.0..1.0
 */
public final class NetworkFoundationsServer {

    private NetworkFoundationsServer() {
    }

    public static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        AtomicInteger threadId = new AtomicInteger(1);
        server.setExecutor(Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r, "nf-http-" + threadId.getAndIncrement());
            t.setDaemon(true);
            return t;
        }));
        server.createContext("/health", new HealthHandler());
        server.createContext("/echo", new EchoHandler());
        server.createContext("/status", new StatusHandler());
        server.createContext("/slow", new SlowHandler());
        server.createContext("/flaky", new FlakyHandler());
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = start(port);
        System.out.println("Network foundations server running on http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
        Thread.currentThread().join();
        server.stop(0);
    }

    private static final class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }
            sendJson(exchange, 200, "{\"status\":\"UP\",\"time\":\"" + Instant.now() + "\"}");
        }
    }

    private static final class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Headers reqHeaders = exchange.getRequestHeaders();
            String requestId = reqHeaders.getFirst("X-Request-Id");
            String contentType = reqHeaders.getFirst("Content-Type");
            String response = "{"
                    + "\"method\":\"" + escape(exchange.getRequestMethod()) + "\","
                    + "\"path\":\"" + escape(exchange.getRequestURI().getPath()) + "\","
                    + "\"requestId\":\"" + escape(orDefault(requestId, "")) + "\","
                    + "\"contentType\":\"" + escape(orDefault(contentType, "")) + "\","
                    + "\"body\":\"" + escape(body) + "\""
                    + "}";
            sendJson(exchange, 200, response);
        }
    }

    private static final class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath(); // /status/404
            String[] parts = path.split("/");
            int code = 200;
            if (parts.length >= 3) {
                try {
                    code = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    code = 400;
                }
            }
            String body = "{\"status\":" + code + ",\"message\":\"simulated\"}";
            sendJson(exchange, code, body);
        }
    }

    private static final class SlowHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            int delayMs = parseIntOrDefault(query.get("delayMs"), 1000);
            sleep(delayMs);
            sendJson(exchange, 200, "{\"delayMs\":" + delayMs + ",\"status\":\"done\"}");
        }
    }

    private static final class FlakyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI());
            double failureRate = parseDoubleOrDefault(query.get("failureRate"), 0.3d);
            double roll = ThreadLocalRandom.current().nextDouble();
            if (roll < failureRate) {
                sendJson(exchange, 503, "{\"error\":\"upstream_unavailable\",\"roll\":" + roll + "}");
                return;
            }
            sendJson(exchange, 200, "{\"status\":\"ok\",\"roll\":" + roll + "}");
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return map;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String k = kv[0];
            String v = kv.length == 2 ? kv[1] : "";
            map.put(k, v);
        }
        return map;
    }

    private static int parseIntOrDefault(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDoubleOrDefault(String value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void sleep(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String orDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
