package com.docverify.engine;

import com.docverify.model.Document;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Builder for generating formatted verification reports in HTML or plain text.
 *
 * OOP Concept: BUILDER PATTERN
 * Allows step-by-step construction of a complex report object via method chaining.
 * The client configures what they want (title, format, detail level) and calls
 * build() to produce the final output — separating construction from representation.
 *
 * Usage:
 * <pre>
 * String html = new ReportBuilder()
 *     .setTitle("Verification Certificate")
 *     .setDocument(doc)
 *     .setReport(report)
 *     .includeDetails(true)
 *     .setFormat("HTML")
 *     .build();
 * </pre>
 */
public class ReportBuilder {

    private String title = "Document Verification Report";
    private Document document;
    private VerificationReport report;
    private boolean includeDetails = true;
    private String format = "HTML"; // "HTML" or "TEXT"

    // ========================
    //    BUILDER METHODS
    // ========================

    /**
     * Sets the report title.
     * @return this builder for chaining
     */
    public ReportBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the document being reported on.
     * @return this builder for chaining
     */
    public ReportBuilder setDocument(Document document) {
        this.document = document;
        return this;
    }

    /**
     * Sets the verification report data.
     * @return this builder for chaining
     */
    public ReportBuilder setReport(VerificationReport report) {
        this.report = report;
        return this;
    }

    /**
     * Controls whether individual rule results are included in the output.
     * @return this builder for chaining
     */
    public ReportBuilder includeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
        return this;
    }

    /**
     * Sets the output format ("HTML" or "TEXT").
     * @return this builder for chaining
     */
    public ReportBuilder setFormat(String format) {
        this.format = format;
        return this;
    }

    // ========================
    //    BUILD METHOD
    // ========================

    /**
     * Builds the report string in the configured format.
     *
     * @return the formatted report content
     * @throws IllegalStateException if document or report is not set
     */
    public String build() {
        if (document == null || report == null) {
            throw new IllegalStateException("Document and Report must be set before building.");
        }

        return switch (format.toUpperCase()) {
            case "HTML" -> buildHtml();
            case "TEXT" -> buildText();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Builds the report and exports it directly to a file.
     *
     * @param outputPath the file path to write the report to
     * @throws IOException if the file cannot be written
     */
    public void exportToFile(Path outputPath) throws IOException {
        String content = build();
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            writer.write(content);
        }
    }

    // ========================
    //    FORMAT BUILDERS
    // ========================

    private String buildHtml() {
        StringBuilder sb = new StringBuilder();
        String verdictColor = switch (report.getVerdict()) {
            case VERIFIED -> "#28a745";
            case SUSPICIOUS -> "#ffc107";
            case REJECTED -> "#dc3545";
        };

        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("  <title>").append(title).append("</title>\n");
        sb.append("  <style>\n");
        sb.append("    * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        sb.append("    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; ");
        sb.append("background: #f4f6f9; padding: 40px; }\n");
        sb.append("    .report { max-width: 700px; margin: 0 auto; background: #fff; ");
        sb.append("border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; }\n");
        sb.append("    .header { background: linear-gradient(135deg, #1a237e, #283593); ");
        sb.append("color: #fff; padding: 30px; text-align: center; }\n");
        sb.append("    .header h1 { font-size: 1.4em; margin-bottom: 5px; }\n");
        sb.append("    .header p { opacity: 0.8; font-size: 0.9em; }\n");
        sb.append("    .verdict { text-align: center; padding: 25px; }\n");
        sb.append("    .verdict-badge { display: inline-block; padding: 12px 40px; ");
        sb.append("border-radius: 30px; font-size: 1.3em; font-weight: bold; color: #fff; }\n");
        sb.append("    .details { padding: 25px; }\n");
        sb.append("    .info-grid { display: grid; grid-template-columns: 140px 1fr; ");
        sb.append("gap: 8px 16px; margin-bottom: 20px; }\n");
        sb.append("    .info-label { font-weight: 600; color: #555; }\n");
        sb.append("    .info-value { color: #222; }\n");
        sb.append("    .rules-table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
        sb.append("    .rules-table th { background: #f0f0f0; padding: 10px 12px; ");
        sb.append("text-align: left; font-size: 0.85em; text-transform: uppercase; color: #666; }\n");
        sb.append("    .rules-table td { padding: 10px 12px; border-bottom: 1px solid #eee; ");
        sb.append("font-size: 0.9em; }\n");
        sb.append("    .pass { color: #28a745; font-weight: 600; }\n");
        sb.append("    .fail { color: #dc3545; font-weight: 600; }\n");
        sb.append("    .summary { background: #f8f9fa; padding: 20px 25px; text-align: center; ");
        sb.append("color: #666; font-size: 0.85em; }\n");
        sb.append("  </style>\n</head>\n<body>\n");

        // Header
        sb.append("  <div class=\"report\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("      <h1>").append(title).append("</h1>\n");
        sb.append("      <p>Generated: ").append(report.getTimestamp().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        sb.append("    </div>\n");

        // Verdict badge
        sb.append("    <div class=\"verdict\">\n");
        sb.append("      <div class=\"verdict-badge\" style=\"background: ")
                .append(verdictColor).append(";\">")
                .append(report.getVerdict().getDisplayName()).append("</div>\n");
        sb.append("    </div>\n");

        // Document details
        sb.append("    <div class=\"details\">\n");
        sb.append("      <div class=\"info-grid\">\n");
        sb.append("        <span class=\"info-label\">Document Type</span>\n");
        sb.append("        <span class=\"info-value\">").append(document.getDocumentType()).append("</span>\n");
        sb.append("        <span class=\"info-label\">Document ID</span>\n");
        sb.append("        <span class=\"info-value\">").append(document.getDocumentId()).append("</span>\n");
        sb.append("        <span class=\"info-label\">Owner</span>\n");
        sb.append("        <span class=\"info-value\">").append(document.getOwnerName()).append("</span>\n");
        sb.append("        <span class=\"info-label\">Issue Date</span>\n");
        sb.append("        <span class=\"info-value\">").append(document.getIssueDate()).append("</span>\n");
        sb.append("        <span class=\"info-label\">Rules Passed</span>\n");
        sb.append("        <span class=\"info-value\">").append(report.getPassedCount())
                .append(" / ").append(report.getResults().size()).append("</span>\n");
        sb.append("      </div>\n");

        // Rule results table
        if (includeDetails) {
            sb.append("      <table class=\"rules-table\">\n");
            sb.append("        <thead><tr><th>Rule</th><th>Status</th><th>Details</th></tr></thead>\n");
            sb.append("        <tbody>\n");
            for (VerificationResult result : report.getResults()) {
                String statusClass = result.isPassed() ? "pass" : "fail";
                String statusText = result.isPassed() ? "PASS" : "FAIL";
                sb.append("          <tr><td>").append(result.getRuleName())
                        .append("</td><td class=\"").append(statusClass).append("\">")
                        .append(statusText).append("</td><td>")
                        .append(result.getMessage()).append("</td></tr>\n");
            }
            sb.append("        </tbody>\n      </table>\n");
        }

        sb.append("    </div>\n");

        // Footer
        sb.append("    <div class=\"summary\">\n");
        sb.append("      Document Verification System v1.0 &mdash; Generated automatically\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n</body>\n</html>\n");

        return sb.toString();
    }

    private String buildText() {
        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(60);
        String thinSeparator = "-".repeat(60);

        sb.append(separator).append("\n");
        sb.append("  ").append(title.toUpperCase()).append("\n");
        sb.append(separator).append("\n\n");

        sb.append("  Document Type : ").append(document.getDocumentType()).append("\n");
        sb.append("  Document ID   : ").append(document.getDocumentId()).append("\n");
        sb.append("  Owner         : ").append(document.getOwnerName()).append("\n");
        sb.append("  Issue Date    : ").append(document.getIssueDate()).append("\n");
        sb.append("  Verified At   : ").append(report.getTimestamp().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        sb.append("  VERDICT: ").append(report.getVerdict().getDisplayName()).append("\n");
        sb.append("  Rules Passed: ").append(report.getPassedCount())
                .append(" / ").append(report.getResults().size()).append("\n\n");

        if (includeDetails) {
            sb.append(thinSeparator).append("\n");
            sb.append("  RULE DETAILS:\n");
            sb.append(thinSeparator).append("\n");
            for (VerificationResult result : report.getResults()) {
                String status = result.isPassed() ? "PASS" : "FAIL";
                sb.append(String.format("  [%s] %-25s %s\n",
                        status, result.getRuleName(), result.getMessage()));
            }
            sb.append(thinSeparator).append("\n");
        }

        sb.append("\n").append(separator).append("\n");
        sb.append("  Document Verification System v1.0\n");
        sb.append(separator).append("\n");

        return sb.toString();
    }
}
