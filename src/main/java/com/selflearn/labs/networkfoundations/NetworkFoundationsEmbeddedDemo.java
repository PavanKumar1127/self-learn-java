package com.selflearn.labs.networkfoundations;

import com.sun.net.httpserver.HttpServer;

/**
 * One-command demo: starts the local server, runs client drills, then shuts down.
 */
public final class NetworkFoundationsEmbeddedDemo {

    private NetworkFoundationsEmbeddedDemo() {
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = NetworkFoundationsServer.start(8080);
        try {
            System.out.println("Embedded server started at http://localhost:8080");
            new NetworkFoundationsClientLab("http://localhost:8080").runAll();
        } finally {
            server.stop(0);
            System.out.println("\nEmbedded server stopped.");
        }
    }
}
