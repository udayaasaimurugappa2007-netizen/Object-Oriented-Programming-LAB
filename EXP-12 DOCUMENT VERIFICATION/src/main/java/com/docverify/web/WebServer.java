package com.docverify.web;

import com.docverify.engine.ReportBuilder;
import com.docverify.engine.VerificationEngine;
import com.docverify.engine.VerificationReport;
import com.docverify.engine.VerificationResult;
import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.factory.DocumentFactory;
import com.docverify.model.Document;
import com.docverify.repository.DocumentRepository;
import com.docverify.repository.JsonDocumentRepository;
import com.docverify.repository.VerificationRecord;
import com.docverify.rules.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP Web Server for the Document Verification System.
 * Serves a modern, interactive web application at http://localhost:8080.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final VerificationEngine engine;
    private final DocumentRepository repository;
    private final DuplicateCheckRule duplicateCheckRule;
    private HttpServer server;

    public WebServer() {
        this.repository = new JsonDocumentRepository(Path.of("verification_history.json"));
        this.engine = new VerificationEngine();

        Set<String> previousIds = repository.loadVerifiedDocumentIds();
        this.duplicateCheckRule = new DuplicateCheckRule(previousIds);
        engine.addRule(new RequiredFieldRule());
        engine.addRule(new DateValidityRule());
        engine.addRule(new FormatPatternRule());
        engine.addRule(new ChecksumIntegrityRule());
        engine.addRule(duplicateCheckRule);
        engine.addRule(new ExpiryRule());
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Routes
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/verify", new VerifyHandler());
        server.createContext("/api/history", new HistoryHandler());
        server.createContext("/api/export", new ExportHandler());

        server.start();
        System.out.println("==============================================================");
        System.out.println("  Document Verification System Web Server is RUNNING!");
        System.out.println("  Access Web Link: http://localhost:" + PORT);
        System.out.println("==============================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer();
            webServer.start();
        } catch (Exception e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    //  HTTP HANDLERS
    // ==========================================

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String html = getIndexHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class VerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readRequestBody(exchange.getRequestBody());
            Map<String, String> payload = gson.fromJson(body, new TypeToken<Map<String, String>>(){}.getType());

            if (payload == null || !payload.containsKey("documentType")) {
                sendJsonResponse(exchange, 400, Map.of("error", "Missing documentType in request"));
                return;
            }

            String docType = payload.get("documentType");

            try {
                Document document = DocumentFactory.createDocument(docType, payload);
                VerificationReport report = engine.runVerification(document);

                // Save to repository
                List<VerificationRecord.RuleResultRecord> ruleRecords = new ArrayList<>();
                for (VerificationResult r : report.getResults()) {
                    ruleRecords.add(new VerificationRecord.RuleResultRecord(
                            r.getRuleName(),
                            r.isPassed(),
                            r.getMessage(),
                            r.getSeverity().getDisplayName()
                    ));
                }

                VerificationRecord record = new VerificationRecord(
                        document.getDocumentId(),
                        document.getDocumentType(),
                        document.getOwnerName(),
                        document.getIssueDate() != null ? document.getIssueDate().toString() : "",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        report.getVerdict().getDisplayName(),
                        report.getResults().size(),
                        (int) report.getPassedCount(),
                        (int) report.getFailedCount(),
                        ruleRecords
                );
                repository.saveRecord(record);

                // Build response
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("success", true);
                resp.put("verdict", report.getVerdict().getDisplayName());
                resp.put("passedCount", report.getPassedCount());
                resp.put("failedCount", report.getFailedCount());
                resp.put("totalRules", report.getResults().size());
                resp.put("summary", report.generateSummary());

                List<Map<String, Object>> resultsList = new ArrayList<>();
                for (VerificationResult r : report.getResults()) {
                    Map<String, Object> rMap = new LinkedHashMap<>();
                    rMap.put("ruleName", r.getRuleName());
                    rMap.put("passed", r.isPassed());
                    rMap.put("message", r.getMessage());
                    rMap.put("severity", r.getSeverity().getDisplayName());
                    resultsList.add(rMap);
                }
                resp.put("rules", resultsList);

                // Generated HTML report
                String htmlReport = new ReportBuilder()
                        .setTitle("Verification Certificate")
                        .setDocument(document)
                        .setReport(report)
                        .includeDetails(true)
                        .setFormat("HTML")
                        .build();
                resp.put("htmlReport", htmlReport);

                sendJsonResponse(exchange, 200, resp);
            } catch (InvalidDocumentException e) {
                sendJsonResponse(exchange, 400, Map.of(
                        "success", false,
                        "error", e.getMessage()
                ));
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, Map.of(
                        "success", false,
                        "error", "Internal server error: " + e.getMessage()
                ));
            }
        }
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<VerificationRecord> records = repository.loadAllRecords();
                sendJsonResponse(exchange, 200, records);
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getRawQuery();
                if (query != null && (query.contains("all=true") || query.equals("all"))) {
                    repository.clearAllRecords();
                    duplicateCheckRule.clearHistory();
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "All records cleared"));
                    return;
                }

                String docId = null;
                if (query != null && query.contains("id=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("id=")) {
                            docId = java.net.URLDecoder.decode(param.substring(3), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }

                if (docId == null || docId.trim().isEmpty()) {
                    String body = readRequestBody(exchange.getRequestBody());
                    if (body != null && !body.isBlank()) {
                        Map<String, Object> map = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                        if (map != null && map.containsKey("documentId")) {
                            docId = Objects.toString(map.get("documentId"), null);
                        }
                    }
                }

                if (docId != null && !docId.trim().isEmpty()) {
                    boolean removed = repository.deleteRecord(docId.trim());
                    duplicateCheckRule.removeDocumentId(docId.trim());
                    sendJsonResponse(exchange, 200, Map.of("success", true, "documentId", docId, "deleted", removed));
                } else {
                    sendJsonResponse(exchange, 400, Map.of("success", false, "error", "Missing documentId or all=true parameter"));
                }
                return;
            }

            sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }

    private class ExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readRequestBody(exchange.getRequestBody());
            Map<String, String> payload = gson.fromJson(body, new TypeToken<Map<String, String>>(){}.getType());

            try {
                String docType = payload.get("documentType");
                Document document = DocumentFactory.createDocument(docType, payload);
                VerificationReport report = engine.runVerification(document);

                String html = new ReportBuilder()
                        .setTitle("Verification Certificate")
                        .setDocument(document)
                        .setReport(report)
                        .includeDetails(true)
                        .setFormat("HTML")
                        .build();

                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"report_" + document.getDocumentId() + ".html\"");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 400, Map.of("error", e.getMessage()));
            }
        }
    }

    private static String readRequestBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String getIndexHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Document Verification System</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg-dark: #07131e;
      --bg-gradient: radial-gradient(circle at 50% 0%, #0d2b45 0%, #08192b 50%, #040d16 100%);
      --card-bg: rgba(10, 26, 44, 0.85);
      --card-border: rgba(56, 189, 248, 0.22);
      --card-border-hover: rgba(56, 189, 248, 0.45);
      --card-inner: #071626;
      
      --accent-sky: #38bdf8;
      --accent-cyan: #06b6d4;
      --accent-light-sky: #7dd3fc;
      --accent-deep-sky: #0284c7;
      --primary-gradient: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
      --primary-gradient-hover: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);

      --text-main: #f0f9ff;
      --text-muted: #94a3b8;
      --text-dim: #64748b;
      --text-warm: #bae6fd;

      --status-pass-bg: rgba(34, 197, 94, 0.16);
      --status-pass-border: rgba(34, 197, 94, 0.35);
      --status-pass-text: #4ade80;

      --status-warn-bg: rgba(245, 158, 11, 0.16);
      --status-warn-border: rgba(245, 158, 11, 0.4);
      --status-warn-text: #fcd34d;

      --status-fail-bg: rgba(239, 68, 68, 0.16);
      --status-fail-border: rgba(239, 68, 68, 0.4);
      --status-fail-text: #f87171;

      --font-sans: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      --font-mono: 'JetBrains Mono', monospace;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
    }

    body {
      background: var(--bg-dark);
      background-image: var(--bg-gradient);
      background-attachment: fixed;
      color: var(--text-main);
      font-family: var(--font-sans);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      line-height: 1.5;
    }

    /* Top Navigation Header */
    header {
      background: rgba(7, 19, 30, 0.9);
      backdrop-filter: blur(14px);
      border-bottom: 1px solid var(--card-border);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .header-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 1.1rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 0.85rem;
      text-decoration: none;
    }

    .brand-icon {
      width: 42px;
      height: 42px;
      border-radius: 10px;
      background: var(--primary-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 1.15rem;
      color: #ffffff;
      box-shadow: 0 4px 18px rgba(2, 132, 199, 0.45);
      border: 1px solid rgba(186, 230, 253, 0.3);
    }

    .brand-text h1 {
      font-size: 1.15rem;
      font-weight: 700;
      color: var(--text-main);
      letter-spacing: -0.01em;
    }

    .nav-tabs {
      display: flex;
      gap: 0.4rem;
      background: rgba(9, 24, 38, 0.85);
      padding: 0.3rem;
      border-radius: 10px;
      border: 1px solid var(--card-border);
    }

    .nav-tab {
      padding: 0.5rem 1.1rem;
      border-radius: 7px;
      border: none;
      background: transparent;
      color: var(--text-dim);
      font-weight: 600;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s ease;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
    }

    .nav-tab:hover {
      color: var(--text-warm);
    }

    .nav-tab.active {
      background: var(--primary-gradient);
      color: #ffffff;
      box-shadow: 0 2px 10px rgba(2, 132, 199, 0.35);
    }

    /* Main Container */
    main {
      flex: 1;
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1.5rem;
      width: 100%;
    }

    /* Standard Grid Layout */
    .grid-container {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1.75rem;
      align-items: start;
    }

    @media (max-width: 960px) {
      .grid-container {
        grid-template-columns: 1fr;
      }
    }

    /* Cards */
    .card {
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      border-radius: 14px;
      padding: 1.75rem;
      box-shadow: 0 12px 36px rgba(0, 0, 0, 0.4);
      backdrop-filter: blur(10px);
      transition: border-color 0.2s;
    }

    .card:hover {
      border-color: var(--card-border-hover);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      padding-bottom: 0.9rem;
      border-bottom: 1px solid var(--card-border);
    }

    .card-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: var(--text-main);
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .card-title-icon {
      color: var(--accent-sky);
    }

    /* Sample Pills */
    .sample-pill-group {
      display: flex;
      gap: 0.4rem;
      flex-wrap: wrap;
    }

    .sample-pill {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.3rem 0.65rem;
      border-radius: 6px;
      border: 1px solid var(--card-border);
      background: rgba(14, 38, 62, 0.7);
      color: var(--text-warm);
      cursor: pointer;
      transition: all 0.15s ease;
    }

    .sample-pill:hover {
      background: rgba(56, 189, 248, 0.22);
      border-color: var(--accent-sky);
      color: #ffffff;
    }

    /* Forms */
    .form-group {
      margin-bottom: 1.15rem;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    @media (max-width: 600px) {
      .form-row {
        grid-template-columns: 1fr;
      }
    }

    label {
      display: block;
      font-size: 0.8rem;
      font-weight: 600;
      color: var(--text-warm);
      margin-bottom: 0.45rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

    input, select {
      width: 100%;
      padding: 0.7rem 0.9rem;
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: 8px;
      color: var(--text-main);
      font-family: var(--font-sans);
      font-size: 0.92rem;
      transition: all 0.2s;
    }

    input:focus, select:focus {
      outline: none;
      border-color: var(--accent-sky);
      background: #0b2238;
      box-shadow: 0 0 0 3px rgba(56, 189, 248, 0.22);
    }

    select option {
      background: #071626;
      color: #f0f9ff;
    }

    /* Buttons */
    .btn-group {
      display: flex;
      gap: 0.75rem;
      margin-top: 1.5rem;
    }

    button.btn {
      padding: 0.7rem 1.3rem;
      border-radius: 8px;
      border: none;
      font-family: var(--font-sans);
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 0.45rem;
      transition: all 0.2s ease;
    }

    .btn-primary {
      background: var(--primary-gradient);
      color: #ffffff;
      box-shadow: 0 4px 14px rgba(2, 132, 199, 0.35);
      flex: 1;
    }

    .btn-primary:hover {
      background: var(--primary-gradient-hover);
      box-shadow: 0 6px 20px rgba(56, 189, 248, 0.4);
      transform: translateY(-1px);
    }

    .btn-secondary {
      background: rgba(14, 38, 62, 0.8);
      color: var(--text-warm);
      border: 1px solid var(--card-border);
    }

    .btn-secondary:hover {
      background: rgba(18, 48, 78, 0.9);
      border-color: var(--accent-sky);
      color: #ffffff;
    }

    /* Verdict Banner */
    .verdict-banner {
      padding: 1.3rem 1.5rem;
      border-radius: 12px;
      text-align: center;
      margin-bottom: 1.25rem;
      display: none;
      animation: fadeIn 0.3s ease-in-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(6px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .verdict-banner.show {
      display: block;
    }

    .verdict-banner.VERIFIED {
      background: var(--status-pass-bg);
      border: 1px solid var(--status-pass-border);
      color: var(--status-pass-text);
    }

    .verdict-banner.SUSPICIOUS {
      background: var(--status-warn-bg);
      border: 1px solid var(--status-warn-border);
      color: var(--status-warn-text);
    }

    .verdict-banner.REJECTED {
      background: var(--status-fail-bg);
      border: 1px solid var(--status-fail-border);
      color: var(--status-fail-text);
    }

    .verdict-title {
      font-size: 1.75rem;
      font-weight: 800;
      letter-spacing: 0.05em;
      margin-bottom: 0.2rem;
    }

    .verdict-stats {
      font-size: 0.88rem;
      font-weight: 500;
      opacity: 0.95;
    }

    /* Rules List */
    .rule-item {
      background: var(--card-inner);
      border: 1px solid var(--card-border);
      border-radius: 10px;
      padding: 0.9rem 1.1rem;
      margin-bottom: 0.65rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      animation: ruleItemIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) backwards;
      transition: border-color 0.2s ease, transform 0.2s ease;
    }

    .rule-item:hover {
      border-color: var(--card-border-hover);
      transform: translateX(2px);
    }

    .rule-info {
      flex: 1;
    }

    .rule-info h4 {
      font-size: 0.92rem;
      font-weight: 600;
      color: var(--text-main);
      margin-bottom: 0.2rem;
    }

    .rule-info p {
      font-size: 0.8rem;
      color: var(--text-muted);
    }

    .badge {
      padding: 0.3rem 0.7rem;
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      white-space: nowrap;
    }

    .badge-pass {
      background: var(--status-pass-bg);
      color: var(--status-pass-text);
      border: 1px solid var(--status-pass-border);
    }

    .badge-fail {
      background: var(--status-fail-bg);
      color: var(--status-fail-text);
      border: 1px solid var(--status-fail-border);
    }

    /* Placeholder Panel */
    .placeholder-panel {
      text-align: center;
      padding: 4rem 1.5rem;
      color: var(--text-dim);
    }

    .placeholder-panel svg {
      width: 54px;
      height: 54px;
      margin-bottom: 1rem;
      stroke: var(--accent-sky);
      opacity: 0.7;
    }

    /* 0.5s DOTTED ROUND EXPANSION TRANSITION */
    .transition-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      pointer-events: none;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
      opacity: 0;
      visibility: hidden;
    }

    .transition-overlay.active {
      visibility: visible;
      opacity: 1;
    }

    .dotted-ring {
      position: absolute;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      border: 3px dotted var(--accent-sky);
      transform: scale(0.1) rotate(0deg);
      opacity: 0;
    }

    .dotted-ring-secondary {
      position: absolute;
      width: 26px;
      height: 26px;
      border-radius: 50%;
      border: 2px dotted var(--accent-light-sky);
      transform: scale(0.1) rotate(0deg);
      opacity: 0;
    }

    .transition-overlay.active .dotted-ring {
      animation: dottedRingExpand 0.5s cubic-bezier(0.16, 0.85, 0.35, 1) forwards;
    }

    .transition-overlay.active .dotted-ring-secondary {
      animation: dottedRingSecondaryExpand 0.5s cubic-bezier(0.16, 0.85, 0.35, 1) forwards;
    }

    @keyframes dottedRingExpand {
      0% {
        transform: scale(0.1) rotate(0deg);
        opacity: 1;
      }
      40% {
        opacity: 0.9;
      }
      70% {
        opacity: 0.5;
      }
      100% {
        transform: scale(26) rotate(180deg);
        opacity: 0;
      }
    }

    @keyframes dottedRingSecondaryExpand {
      0% {
        transform: scale(0.1) rotate(0deg);
        opacity: 1;
      }
      40% {
        opacity: 0.75;
      }
      70% {
        opacity: 0.35;
      }
      100% {
        transform: scale(18) rotate(-180deg);
        opacity: 0;
      }
    }

    /* Page View Transitions: in between 0.5s sequence */
    .view-page {
      display: none;
      opacity: 0;
      transform: translateY(14px);
    }

    .view-page.active {
      display: block;
      animation: pageTransitionIn 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }

    .view-page.fade-out {
      display: block;
      animation: pageTransitionOut 0.22s cubic-bezier(0.4, 0, 1, 1) forwards;
    }

    @keyframes pageTransitionIn {
      0% {
        opacity: 0;
        transform: translateY(14px) scale(0.99);
      }
      100% {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
    }

    @keyframes pageTransitionOut {
      0% {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
      100% {
        opacity: 0;
        transform: translateY(-10px) scale(0.99);
      }
    }

    .field-transition {
      animation: fieldFadeIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes fieldFadeIn {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes ruleItemIn {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    tbody tr {
      animation: rowFadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes rowFadeIn {
      from {
        opacity: 0;
        transform: translateY(6px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .table-container {
      overflow-x: auto;
      border-radius: 8px;
      border: 1px solid var(--card-border);
    }

    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }

    th, td {
      padding: 0.85rem 1rem;
      border-bottom: 1px solid var(--card-border);
      font-size: 0.88rem;
    }

    th {
      background: rgba(9, 26, 44, 0.95);
      font-size: 0.75rem;
      text-transform: uppercase;
      color: var(--text-warm);
      font-weight: 700;
      letter-spacing: 0.05em;
    }

    td {
      background: var(--card-inner);
      color: var(--text-main);
    }

    tr:last-child td {
      border-bottom: none;
    }

    tr:hover td {
      background: rgba(14, 38, 62, 0.5);
    }

    .doc-id-pill {
      font-family: var(--font-mono);
      font-weight: 600;
      color: var(--accent-sky);
      background: rgba(56, 189, 248, 0.12);
      padding: 0.2rem 0.5rem;
      border-radius: 4px;
      border: 1px solid rgba(56, 189, 248, 0.25);
    }

    .btn-delete {
      background: rgba(239, 68, 68, 0.15);
      color: #fca5a5;
      border: 1px solid rgba(239, 68, 68, 0.35);
      padding: 0.3rem 0.65rem;
      border-radius: 6px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      transition: all 0.2s ease;
    }

    .btn-delete:hover {
      background: rgba(239, 68, 68, 0.3);
      border-color: #ef4444;
      color: #ffffff;
      transform: translateY(-1px);
    }

    .btn-danger-outline {
      background: rgba(239, 68, 68, 0.12);
      color: #fca5a5;
      border: 1px solid rgba(239, 68, 68, 0.35);
    }

    .btn-danger-outline:hover {
      background: rgba(239, 68, 68, 0.25);
      border-color: #ef4444;
      color: #ffffff;
    }

    /* Footer */
    footer {
      text-align: center;
      padding: 1.5rem;
      color: var(--text-dim);
      font-size: 0.8rem;
      border-top: 1px solid var(--card-border);
      background: rgba(7, 19, 30, 0.7);
      margin-top: auto;
    }
  </style>
</head>
<body>

  <!-- DOTTED ROUND PAGE TRANSITION OVERLAY -->
  <div id="transitionOverlay" class="transition-overlay" aria-hidden="true">
    <div class="dotted-ring"></div>
    <div class="dotted-ring-secondary"></div>
  </div>

  <!-- HEADER -->
  <header>
    <div class="header-container">
      <div class="brand">
        <div class="brand-text">
          <h1>Document Verification System</h1>
        </div>
      </div>
      <div class="nav-tabs">
        <button class="nav-tab active" id="tabVerify" onclick="switchTab('verify')">Verify Document</button>
        <button class="nav-tab" id="tabHistory" onclick="switchTab('history')">Verification History</button>
      </div>
    </div>
  </header>

  <!-- MAIN CONTENT -->
  <main>
    <!-- VERIFY VIEW -->
    <div class="view-page active" id="verifyView">
      <div class="grid-container">
        
        <!-- LEFT: INPUT FORM -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">Document Details</div>
            <div class="sample-pill-group">
              <button type="button" class="sample-pill" onclick="loadSample('valid-id')">Sample Valid ID</button>
              <button type="button" class="sample-pill" onclick="loadSample('expired-id')">Sample Expired</button>
              <button type="button" class="sample-pill" onclick="loadSample('valid-cert')">Sample Cert</button>
            </div>
          </div>

          <form id="verifyForm" onsubmit="event.preventDefault(); submitVerification();">
            <div class="form-group">
              <label for="docType">Document Type</label>
              <select id="docType" onchange="toggleFields()">
                <option value="IDCARD">ID Card Document</option>
                <option value="CERTIFICATE">Certificate Document</option>
              </select>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="docId">Document ID</label>
                <input type="text" id="docId" required placeholder="e.g. DOC-101">
              </div>
              <div class="form-group">
                <label for="ownerName">Owner Full Name</label>
                <input type="text" id="ownerName" required placeholder="e.g. Rajesh Sharma">
              </div>
            </div>

            <div class="form-group">
              <label for="issueDate">Issue Date</label>
              <input type="date" id="issueDate" required>
            </div>

            <!-- ID Card Specific Fields -->
            <div id="idCardFields">
              <div class="form-row">
                <div class="form-group">
                  <label for="idNumber">ID Number (ID-YYYY-NNNN)</label>
                  <input type="text" id="idNumber" placeholder="ID-2024-001234">
                </div>
                <div class="form-group">
                  <label for="expiryDate">Expiry Date</label>
                  <input type="date" id="expiryDate">
                </div>
              </div>
            </div>

            <!-- Certificate Specific Fields -->
            <div id="certFields" style="display: none;">
              <div class="form-group">
                <label for="certNumber">Certificate Number (CERT-YYYY-XXX-NNN)</label>
                <input type="text" id="certNumber" placeholder="CERT-2024-IIT-101">
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label for="authority">Issuing Authority</label>
                  <input type="text" id="authority" placeholder="e.g. IIT Bombay">
                </div>
                <div class="form-group">
                  <label for="grade">Grade / Distinction</label>
                  <input type="text" id="grade" placeholder="e.g. Distinction">
                </div>
              </div>
            </div>

            <div class="btn-group">
              <button type="submit" class="btn btn-primary" id="btnSubmit">
                <span>Run Verification Pipeline</span>
              </button>
              <button type="button" class="btn btn-secondary" onclick="clearForm()">Clear</button>
            </div>
          </form>
        </div>

        <!-- RIGHT: RESULTS -->
        <div class="card" id="resultsCard">
          <div class="card-header">
            <div class="card-title">Verification Result</div>
            <button class="btn btn-secondary" id="btnExport" style="display: none; padding: 0.35rem 0.8rem; font-size: 0.8rem;" onclick="downloadReport()">Export Report</button>
          </div>

          <div id="placeholderPanel" class="placeholder-panel">
            <p>Fill in the form on the left or select a sample to execute the polymorphic verification pipeline.</p>
          </div>

          <div id="resultContent" style="display: none;">
            <div id="verdictBanner" class="verdict-banner">
              <div class="verdict-title" id="verdictTitle">VERIFIED</div>
              <div class="verdict-stats" id="verdictStats">Rules Passed: 6 / 6 | Failures: 0</div>
            </div>

            <div id="rulesList"></div>
          </div>
        </div>

      </div>
    </div>

    <!-- HISTORY VIEW -->
    <div class="view-page" id="historyView">
      <div class="card">
        <div class="card-header">
          <div class="card-title">Verification Audit History</div>
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
            <button class="btn btn-secondary" style="padding: 0.4rem 0.9rem; font-size: 0.85rem;" onclick="loadHistory()">Refresh Records</button>
            <button class="btn btn-danger-outline" style="padding: 0.4rem 0.9rem; font-size: 0.85rem;" onclick="clearAllHistory()">Clear All History</button>
          </div>
        </div>
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Document ID</th>
                <th>Type</th>
                <th>Owner Name</th>
                <th>Verification Date</th>
                <th>Verdict</th>
                <th>Score</th>
                <th style="text-align: center;">Action</th>
              </tr>
            </thead>
            <tbody id="historyTableBody">
              <tr><td colspan="7" style="text-align: center; color: var(--text-dim); padding: 2rem;">Loading audit records...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </main>

  <footer>
    Document Verification System | Enterprise OOP Architecture | Local Server
  </footer>

  <script>
    let currentPayload = null;

    function toggleFields() {
      const type = document.getElementById('docType').value;
      const idFields = document.getElementById('idCardFields');
      const certFields = document.getElementById('certFields');
      if (type === 'IDCARD') {
        certFields.style.display = 'none';
        certFields.classList.remove('field-transition');
        idFields.style.display = 'block';
        idFields.classList.add('field-transition');
      } else {
        idFields.style.display = 'none';
        idFields.classList.remove('field-transition');
        certFields.style.display = 'block';
        certFields.classList.add('field-transition');
      }
    }

    function clearForm() {
      document.getElementById('verifyForm').reset();
      toggleFields();
    }

    function loadSample(kind) {
      if (kind === 'valid-id') {
        document.getElementById('docType').value = 'IDCARD';
        document.getElementById('docId').value = 'ID-' + Math.floor(1000 + Math.random() * 9000);
        document.getElementById('ownerName').value = 'Aarav Sharma';
        document.getElementById('issueDate').value = '2023-05-15';
        document.getElementById('idNumber').value = 'ID-2023-987654';
        document.getElementById('expiryDate').value = '2028-05-15';
      } else if (kind === 'expired-id') {
        document.getElementById('docType').value = 'IDCARD';
        document.getElementById('docId').value = 'EXP-' + Math.floor(1000 + Math.random() * 9000);
        document.getElementById('ownerName').value = 'Priya Patel';
        document.getElementById('issueDate').value = '2015-01-10';
        document.getElementById('idNumber').value = 'ID-2015-123456';
        document.getElementById('expiryDate').value = '2020-01-10';
      } else if (kind === 'valid-cert') {
        document.getElementById('docType').value = 'CERTIFICATE';
        document.getElementById('docId').value = 'CERT-' + Math.floor(1000 + Math.random() * 9000);
        document.getElementById('ownerName').value = 'Rohan Verma';
        document.getElementById('issueDate').value = '2023-08-20';
        document.getElementById('certNumber').value = 'CERT-2023-IIT-891';
        document.getElementById('authority').value = 'IIT Bombay';
        document.getElementById('grade').value = 'Distinction';
      }
      toggleFields();
    }

    async function submitVerification() {
      const type = document.getElementById('docType').value;
      const payload = {
        documentType: type,
        documentId: document.getElementById('docId').value,
        ownerName: document.getElementById('ownerName').value,
        issueDate: document.getElementById('issueDate').value,
      };

      if (type === 'IDCARD') {
        payload.idNumber = document.getElementById('idNumber').value;
        payload.expiryDate = document.getElementById('expiryDate').value;
      } else {
        payload.certificateNumber = document.getElementById('certNumber').value;
        payload.issuingAuthority = document.getElementById('authority').value;
        payload.grade = document.getElementById('grade').value;
      }

      currentPayload = payload;

      try {
        const res = await fetch('/api/verify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        renderResults(data);
      } catch (err) {
        alert('Verification failed: ' + err.message);
      }
    }

    function renderResults(data) {
      document.getElementById('placeholderPanel').style.display = 'none';
      document.getElementById('resultContent').style.display = 'block';
      document.getElementById('btnExport').style.display = 'inline-flex';

      const banner = document.getElementById('verdictBanner');
      banner.className = 'verdict-banner show ' + data.verdict;
      document.getElementById('verdictTitle').textContent = data.verdict;
      document.getElementById('verdictStats').textContent = 
        `Rules Passed: ${data.passedCount} / ${data.totalRules} | Failures: ${data.failedCount}`;

      const list = document.getElementById('rulesList');
      list.innerHTML = '';

      data.rules.forEach((r, idx) => {
        const item = document.createElement('div');
        item.className = 'rule-item';
        item.style.animationDelay = (idx * 0.05) + 's';
        item.innerHTML = `
          <div class="rule-info">
            <h4>${r.ruleName}</h4>
            <p>${r.message}</p>
          </div>
          <span class="badge ${r.passed ? 'badge-pass' : 'badge-fail'}">
            ${r.passed ? 'PASS' : r.severity}
          </span>
        `;
        list.appendChild(item);
      });
    }

    async function downloadReport() {
      if (!currentPayload) return;
      const res = await fetch('/api/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(currentPayload)
      });
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${currentPayload.documentId}.html`;
      document.body.appendChild(a);
      a.click();
      a.remove();
    }

    async function loadHistory() {
      const tbody = document.getElementById('historyTableBody');
      tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-dim); padding: 1.5rem;">Loading history...</td></tr>';
      try {
        const res = await fetch('/api/history');
        const records = await res.json();
        if (!records || !records.length) {
          tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-dim); padding: 2rem;">No previous verification records found.</td></tr>';
          return;
        }
        tbody.innerHTML = records.map(r => `
          <tr>
            <td><span class="doc-id-pill">${r.documentId}</span></td>
            <td>${r.documentType}</td>
            <td>${r.ownerName}</td>
            <td style="color: var(--text-muted); font-size: 0.82rem;">${r.verificationTimestamp}</td>
            <td><span class="badge ${r.verdict === 'VERIFIED' ? 'badge-pass' : 'badge-fail'}">${r.verdict}</span></td>
            <td style="font-weight: 600;">${r.passedRules}/${r.totalRules}</td>
            <td style="text-align: center;">
              <button class="btn-delete" onclick="deleteRecord('${r.documentId}')">Delete</button>
            </td>
          </tr>
        `).reverse().join('');
      } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="color: var(--status-fail-text); padding: 1.5rem; text-align: center;">Failed to load history: ${err.message}</td></tr>`;
      }
    }

    async function deleteRecord(docId) {
      if (!confirm(`Delete verification record for '${docId}'?`)) {
        return;
      }
      try {
        const res = await fetch('/api/history?id=' + encodeURIComponent(docId), { method: 'DELETE' });
        const result = await res.json();
        if (result.success) {
          await loadHistory();
        } else {
          alert('Failed to delete: ' + (result.error || 'Unknown error'));
        }
      } catch (err) {
        alert('Error deleting record: ' + err.message);
      }
    }

    async function clearAllHistory() {
      if (!confirm('Are you sure you want to delete ALL verification history records? This cannot be undone.')) {
        return;
      }
      try {
        const res = await fetch('/api/history?all=true', { method: 'DELETE' });
        const result = await res.json();
        if (result.success) {
          await loadHistory();
        } else {
          alert('Failed to clear records: ' + (result.error || 'Unknown error'));
        }
      } catch (err) {
        alert('Error clearing records: ' + err.message);
      }
    }

    let isTransitioning = false;

    function switchTab(tab) {
      if (isTransitioning) return;
      const tabVerify = document.getElementById('tabVerify');
      const tabHistory = document.getElementById('tabHistory');
      const verifyView = document.getElementById('verifyView');
      const historyView = document.getElementById('historyView');
      const overlay = document.getElementById('transitionOverlay');

      const isToVerify = (tab === 'verify');
      const currentView = isToVerify ? historyView : verifyView;
      const nextView = isToVerify ? verifyView : historyView;

      if (nextView.classList.contains('active')) return;

      isTransitioning = true;

      // Update active tab buttons
      if (isToVerify) {
        tabHistory.classList.remove('active');
        tabVerify.classList.add('active');
      } else {
        tabVerify.classList.remove('active');
        tabHistory.classList.add('active');
      }

      // 1. Activate expanding dotted round overlay (lasts 0.5s)
      overlay.classList.remove('active');
      void overlay.offsetWidth;
      overlay.classList.add('active');

      // 2. Fade out current view during the first ~0.22s
      currentView.classList.add('fade-out');

      // 3. In between the transition (~240ms), switch views and make next page appear
      setTimeout(() => {
        currentView.classList.remove('active', 'fade-out');
        nextView.classList.add('active');
        void nextView.offsetWidth;
        if (!isToVerify) {
          loadHistory();
        }
      }, 240);

      // 4. Conclude transition sequence at exactly 0.5s (500ms)
      setTimeout(() => {
        overlay.classList.remove('active');
        isTransitioning = false;
      }, 500);
    }

    // Default sample on initial load
    loadSample('valid-id');
  </script>
</body>
</html>
""";
    }
}
