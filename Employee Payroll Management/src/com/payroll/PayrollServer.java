package com.payroll;

import com.payroll.model.Employee;
import com.payroll.service.PayrollService;
import com.payroll.util.SimpleJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Built-in Lightweight HTTP Server serving both REST API and Frontend static assets.
 */
public class PayrollServer {
    public static final int PORT = 8080;
    private final PayrollService payrollService;
    private final File publicDir;

    public PayrollServer() {
        this.payrollService = new PayrollService();
        this.publicDir = new File("public");
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/employees", new EmployeesHandler());
        server.createContext("/api/payroll", new PayrollHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("=======================================================");
        System.out.println("  🌟 PAYROLL PULSE - EMPLOYEE PAYROLL MANAGEMENT SYSTEM ");
        System.out.println("=======================================================");
        System.out.println("  Server is running at: http://localhost:" + PORT + "/");
        System.out.println("  Press Ctrl+C to stop the server.");
        System.out.println("=======================================================");
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        addCorsHeaders(exchange);
        String json = SimpleJson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    String k = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String v = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    map.put(k, v);
                } else {
                    map.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    // --- Handlers ---

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                addCorsHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                sendJsonResponse(exchange, 404, Map.of("error", "API endpoint not found: " + path));
                return;
            }

            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File requestedFile = new File(publicDir, path.replace('/', File.separatorChar));
            // Prevent directory traversal
            if (!requestedFile.getCanonicalPath().startsWith(publicDir.getCanonicalPath())) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            if (!requestedFile.exists() || requestedFile.isDirectory()) {
                // Fallback to index.html for SPA routing
                requestedFile = new File(publicDir, "index.html");
                if (!requestedFile.exists()) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
            }

            String mime = getMimeType(requestedFile.getName());
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", mime);
            byte[] content = Files.readAllBytes(requestedFile.toPath());
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private String getMimeType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "application/octet-stream";
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            sendJsonResponse(exchange, 200, Map.of(
                    "status", "UP",
                    "system", "Employee Payroll Management Engine",
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    private class EmployeesHandler implements HttpHandler {
        @Override
        @SuppressWarnings("unchecked")
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath(); // /api/employees or /api/employees/{id}
            String subPath = path.substring("/api/employees".length());
            if (subPath.startsWith("/")) subPath = subPath.substring(1);

            try {
                if ("GET".equals(method)) {
                    if (subPath.isEmpty()) {
                        sendJsonResponse(exchange, 200, payrollService.getAllEmployees());
                    } else {
                        int id = Integer.parseInt(subPath);
                        Employee emp = payrollService.getEmployee(id);
                        if (emp == null) {
                            sendJsonResponse(exchange, 404, Map.of("error", "Employee not found"));
                        } else {
                            sendJsonResponse(exchange, 200, emp.toMap());
                        }
                    }
                } else if ("POST".equals(method)) {
                    String body = readRequestBody(exchange);
                    Object parsed = SimpleJson.parse(body);
                    if (parsed instanceof Map<?, ?>) {
                        Employee created = payrollService.addEmployee((Map<String, Object>) parsed);
                        sendJsonResponse(exchange, 201, Map.of("success", true, "employee", created.toMap()));
                    } else {
                        sendJsonResponse(exchange, 400, Map.of("error", "Invalid JSON payload"));
                    }
                } else if ("PUT".equals(method)) {
                    if (subPath.isEmpty()) {
                        sendJsonResponse(exchange, 400, Map.of("error", "Employee ID required in URL"));
                        return;
                    }
                    int id = Integer.parseInt(subPath);
                    String body = readRequestBody(exchange);
                    Object parsed = SimpleJson.parse(body);
                    if (parsed instanceof Map<?, ?>) {
                        Employee updated = payrollService.updateEmployee(id, (Map<String, Object>) parsed);
                        if (updated != null) {
                            sendJsonResponse(exchange, 200, Map.of("success", true, "employee", updated.toMap()));
                        } else {
                            sendJsonResponse(exchange, 404, Map.of("error", "Employee not found"));
                        }
                    } else {
                        sendJsonResponse(exchange, 400, Map.of("error", "Invalid JSON payload"));
                    }
                } else if ("DELETE".equals(method)) {
                    if (subPath.isEmpty()) {
                        sendJsonResponse(exchange, 400, Map.of("error", "Employee ID required in URL"));
                        return;
                    }
                    int id = Integer.parseInt(subPath);
                    boolean deleted = payrollService.deleteEmployee(id);
                    if (deleted) {
                        sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Employee removed"));
                    } else {
                        sendJsonResponse(exchange, 404, Map.of("error", "Employee not found"));
                    }
                } else {
                    sendJsonResponse(exchange, 405, Map.of("error", "Method not allowed"));
                }
            } catch (Exception ex) {
                sendJsonResponse(exchange, 500, Map.of("error", ex.getMessage()));
            }
        }
    }

    private class PayrollHandler implements HttpHandler {
        @Override
        @SuppressWarnings("unchecked")
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());

            try {
                if ("GET".equals(method)) {
                    String month = query.getOrDefault("month", "ALL");
                    sendJsonResponse(exchange, 200, payrollService.getPayrollRecords(month));
                } else if ("POST".equals(method)) {
                    String body = readRequestBody(exchange);
                    Object parsed = SimpleJson.parse(body);
                    Map<String, Object> map = (parsed instanceof Map<?, ?>) ? (Map<String, Object>) parsed : Collections.emptyMap();

                    if (path.endsWith("/process")) {
                        String month = String.valueOf(map.getOrDefault("month", "2026-10"));
                        int workingDays = 22;
                        if (map.containsKey("workingDays")) {
                            workingDays = ((Number) map.get("workingDays")).intValue();
                        }
                        Map<String, Object> overrides = (map.get("overrides") instanceof Map<?, ?>)
                                ? (Map<String, Object>) map.get("overrides") : Collections.emptyMap();

                        payrollService.processPayroll(month, workingDays, overrides);
                        sendJsonResponse(exchange, 200, Map.of(
                                "success", true,
                                "message", "Payroll processed successfully for " + month,
                                "records", payrollService.getPayrollRecords(month)
                        ));
                    } else if (path.endsWith("/status")) {
                        String id = String.valueOf(map.get("id"));
                        String status = String.valueOf(map.get("status"));
                        boolean updated = payrollService.updatePayrollStatus(id, status);
                        if (updated) {
                            sendJsonResponse(exchange, 200, Map.of("success", true));
                        } else {
                            sendJsonResponse(exchange, 404, Map.of("error", "Payroll record not found"));
                        }
                    } else {
                        sendJsonResponse(exchange, 400, Map.of("error", "Unknown payroll action"));
                    }
                } else {
                    sendJsonResponse(exchange, 405, Map.of("error", "Method not allowed"));
                }
            } catch (Exception ex) {
                sendJsonResponse(exchange, 500, Map.of("error", ex.getMessage()));
            }
        }
    }

    private class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());
            String month = query.getOrDefault("month", "ALL");
            sendJsonResponse(exchange, 200, payrollService.getAnalytics(month));
        }
    }

    public static void main(String[] args) {
        try {
            PayrollServer server = new PayrollServer();
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
