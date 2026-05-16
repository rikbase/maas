package com.maas.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maas.mcp.entity.McpServer;
import com.maas.mcp.entity.McpTransport;
import com.maas.mcp.repository.McpServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class McpRuntimeClient {
    private static final Logger log = LoggerFactory.getLogger(McpRuntimeClient.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    private final McpServerRepository serverRepository;

    // Active connections: serverId -> process
    private final ConcurrentHashMap<UUID, McpConnection> connections = new ConcurrentHashMap<>();

    public McpRuntimeClient(McpServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    /**
     * List available tools from an MCP server.
     */
    public McpToolListResult listTools(UUID serverId) {
        McpServer server = serverRepository.findById(serverId)
            .orElseThrow(() -> new RuntimeException("MCP server not found: " + serverId));
        return listTools(server);
    }

    public McpToolListResult listTools(McpServer server) {
        try {
            McpConnection conn = getOrCreateConnection(server);
            return sendRequest(conn, "tools/list", null, McpToolListResult.class);
        } catch (Exception e) {
            log.error("Failed to list tools from MCP server {}: {}", server.getName(), e.getMessage());
            throw new RuntimeException("Failed to list MCP tools: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a tool call on an MCP server.
     */
    public McpToolCallResult executeTool(UUID serverId, String toolName, Map<String, Object> arguments) {
        McpServer server = serverRepository.findById(serverId)
            .orElseThrow(() -> new RuntimeException("MCP server not found: " + serverId));

        try {
            McpConnection conn = getOrCreateConnection(server);
            Map<String, Object> params = new HashMap<>();
            params.put("name", toolName);
            params.put("arguments", arguments);
            return sendRequest(conn, "tools/call", params, McpToolCallResult.class);
        } catch (Exception e) {
            log.error("Failed to execute tool {} on MCP server {}: {}", toolName, server.getName(), e.getMessage());
            throw new RuntimeException("Failed to execute MCP tool: " + e.getMessage(), e);
        }
    }

    /**
     * Disconnect from an MCP server.
     */
    public void disconnect(UUID serverId) {
        McpConnection conn = connections.remove(serverId);
        if (conn != null) {
            conn.close();
        }
    }

    public void disconnectAll() {
        connections.values().forEach(McpConnection::close);
        connections.clear();
    }

    private McpConnection getOrCreateConnection(McpServer server) throws IOException {
        UUID id = server.getId();

        // Check for existing connection
        McpConnection existing = connections.get(id);
        if (existing != null && existing.isAlive()) {
            return existing;
        }

        // Create new connection
        McpConnection conn = createConnection(server);
        connections.put(id, conn);

        // Initialize session
        initializeConnection(conn);
        return conn;
    }

    private McpConnection createConnection(McpServer server) throws IOException {
        if (server.getTransport() == McpTransport.sse) {
            return new SseConnection(server, httpClient, objectMapper);
        } else {
            // stdio transport
            return new StdioConnection(server, objectMapper);
        }
    }

    private void initializeConnection(McpConnection conn) throws IOException {
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("name", "maas-gateway");
        clientInfo.put("version", "1.0.0");

        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", "2024-11-05");
        params.put("capabilities", Collections.emptyMap());
        params.put("clientInfo", clientInfo);

        sendRequest(conn, "initialize", params, JsonNode.class);
    }

    private <T> T sendRequest(McpConnection conn, String method, Map<String, Object> params, Class<T> responseType)
            throws IOException {
        int id = conn.nextId();
        ObjectNode request = objectMapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", method);
        request.set("params", params != null ? objectMapper.valueToTree(params) : objectMapper.createObjectNode());

        String requestJson = objectMapper.writeValueAsString(request);
        log.debug("MCP request: {}", requestJson);

        String responseJson = conn.sendAndReceive(requestJson);
        log.debug("MCP response: {}", responseJson);

        JsonNode response = objectMapper.readTree(responseJson);

        if (response.has("error") && !response.get("error").isNull()) {
            JsonNode error = response.get("error");
            throw new RuntimeException("MCP error: " + error.get("message").asText()
                + " (code=" + error.get("code").asInt() + ")");
        }

        JsonNode result = response.get("result");
        if (result == null) {
            throw new RuntimeException("MCP response missing result");
        }

        return objectMapper.treeToValue(result, responseType);
    }

    // ---- Connection abstractions ----

    interface McpConnection {
        int nextId();
        String sendAndReceive(String message) throws IOException;
        boolean isAlive();
        void close();
    }

    static class StdioConnection implements McpConnection {
        private final Process process;
        private final BufferedReader reader;
        private final BufferedWriter writer;
        private final AtomicInteger requestId = new AtomicInteger(1);
        private volatile boolean alive = true;

        StdioConnection(McpServer server, ObjectMapper objectMapper) throws IOException {
            List<String> cmd = new ArrayList<>();
            if (server.getCommand() != null) {
                cmd.add(server.getCommand());
            }
            if (server.getArgs() != null && !server.getArgs().equals("[]")) {
                try {
                    JsonNode args = objectMapper.readTree(server.getArgs());
                    if (args.isArray()) {
                        args.forEach(a -> cmd.add(a.asText()));
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse MCP args JSON: {}", e.getMessage());
                }
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (server.getEnvJson() != null && !server.getEnvJson().equals("{}")) {
                try {
                    JsonNode env = objectMapper.readTree(server.getEnvJson());
                    env.fieldNames().forEachRemaining(k -> {
                        String v = env.get(k).asText();
                        if (v != null) pb.environment().put(k, v);
                    });
                } catch (Exception e) {
                    log.warn("Failed to parse MCP env JSON: {}", e.getMessage());
                }
            }

            this.process = pb.start();
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        }

        @Override
        public int nextId() { return requestId.getAndIncrement(); }

        @Override
        public String sendAndReceive(String message) throws IOException {
            writer.write(message);
            writer.newLine();
            writer.flush();
            return reader.readLine();
        }

        @Override
        public boolean isAlive() {
            return alive && process.isAlive();
        }

        @Override
        public void close() {
            alive = false;
            try { writer.close(); } catch (Exception ignored) {}
            try { reader.close(); } catch (Exception ignored) {}
            process.destroyForcibly();
        }
    }

    static class SseConnection implements McpConnection {
        private final McpServer server;
        private final HttpClient httpClient;
        private final AtomicInteger requestId = new AtomicInteger(1);
        private volatile boolean alive = true;

        SseConnection(McpServer server, HttpClient httpClient, ObjectMapper objectMapper) {
            this.server = server;
            this.httpClient = httpClient;
        }

        @Override
        public int nextId() { return requestId.getAndIncrement(); }

        @Override
        public String sendAndReceive(String message) throws IOException {
            try {
                String url = server.getUrl();
                if (url == null || url.isBlank()) {
                    throw new IOException("SSE transport requires a URL");
                }

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", server.getApiKey() != null
                        ? "Bearer " + server.getApiKey() : "")
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .timeout(Duration.ofSeconds(30))
                    .build();

                HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("SSE request failed: " + response.statusCode());
                }

                return response.body();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("SSE request interrupted", e);
            }
        }

        @Override
        public boolean isAlive() { return alive; }

        @Override
        public void close() { alive = false; }
    }

    // ---- Result types ----

    public record McpToolDefinition(String name, String description, JsonNode inputSchema) {}

    public record McpToolListResult(List<McpToolDefinition> tools) {}

    public record McpToolCallResult(JsonNode content, Boolean isError) {}
}
