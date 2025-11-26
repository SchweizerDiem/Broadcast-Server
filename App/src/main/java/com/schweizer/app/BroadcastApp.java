package com.schweizer.app;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.client.WebSocketClient;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BroadcastApp {

    private static final int PORT = 8887;
    private static final String SERVER_URI = "ws://localhost:" + PORT;

    /**
     * Main method to handle CLI commands: "broadcast-server start" or "broadcast-server connect"
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2 || !args[0].equals("broadcast-server")) {
            System.out.println("Usage: java BroadcastApp broadcast-server [start|connect]");
            return;
        }

        String command = args[1];

        if (command.equals("start")) {
            startServer();
        } else if (command.equals("connect")) {
            startClient();
        } else {
            System.out.println("Invalid command: " + command);
            System.out.println("Usage: java BroadcastApp broadcast-server [start|connect]");
        }
    }

    // --- Server Implementation ---

    public static class SimpleBroadcastServer extends WebSocketServer {
        // Thread-safe set to store connected clients (WebSockets)
        private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

        public SimpleBroadcastServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            connections.add(conn);
            String clientInfo = conn.getRemoteSocketAddress().toString();
            System.out.println("✅ New client connected: " + clientInfo);
            broadcast("A new client has joined the chat: " + clientInfo); // Notify others
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            connections.remove(conn);
            String clientInfo = conn.getRemoteSocketAddress().toString();
            System.out.println("❌ Client disconnected: " + clientInfo + " (Code: " + code + ", Reason: " + reason + ")");
            broadcast("A client has left the chat: " + clientInfo); // Notify others
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            String senderInfo = conn.getRemoteSocketAddress().toString();
            System.out.println("✉️ Received message from " + senderInfo + ": " + message);
            
            // Broadcast the received message to ALL connected clients
            String broadcastMessage = "[" + senderInfo + "] " + message;
            broadcast(broadcastMessage);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            if (conn != null) {
                connections.remove(conn);
                System.err.println("🚨 Server error for client " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
            } else {
                System.err.println("🚨 Server error: " + ex.getMessage());
            }
        }

        @Override
        public void onStart() {
            System.out.println("🚀 Server started successfully on port " + getPort());
            setConnectionLostTimeout(100); // Enable ping/pong for graceful disconnections
        }

        // Overrides the default `broadcast` to use our managed set
        @Override
        public void broadcast(String text) {
            synchronized (connections) {
                for (WebSocket conn : connections) {
                    conn.send(text);
                }
            }
        }
    }

    private static void startServer() throws InterruptedException, IOException {
        InetSocketAddress address = new InetSocketAddress(PORT);
        SimpleBroadcastServer server = new SimpleBroadcastServer(address);
        server.start();

        System.out.println("\nPress ENTER to stop the server...");
        new BufferedReader(new InputStreamReader(System.in)).readLine();
        
        System.out.println("🛑 Stopping server...");
        server.stop(1000); // Graceful shutdown with 1 second timeout
        System.out.println("Server stopped.");
    }

    // --- Client Implementation ---

    private static void startClient() throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("Enter your username/alias: ");
        final String username = reader.readLine();

        WebSocketClient client = new WebSocketClient(new URI(SERVER_URI)) {

            @Override
            public void onOpen(org.java_websocket.handshake.ServerHandshake handshakedata) {
                System.out.println("✅ Connected to the server: " + getURI());
                send(username + " has connected.");
            }

            @Override
            public void onMessage(String message) {
                // All messages from the server are broadcast messages
                System.out.println("\n<BROADCAST> " + message);
                System.out.print("Your message: "); // Re-prompt the user
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("\n❌ Disconnected from server. Code: " + code + ", Reason: " + reason);
                System.exit(0);
            }

            @Override
            public void onError(Exception ex) {
                System.err.println("🚨 Client error: " + ex.getMessage());
            }
        };

        // Attempt to connect
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Connection interrupted.");
            return;
        }

        // Start reading user input to send messages
        System.out.println("You can now send messages. Type 'exit' to disconnect.");
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("exit")) {
                client.close();
                break;
            }
            if (client.isOpen()) {
                client.send(username + ": " + line);
            } else {
                System.out.println("Connection closed. Cannot send message.");
                break;
            }
            System.out.print("Your message: ");
        }
    }
}
