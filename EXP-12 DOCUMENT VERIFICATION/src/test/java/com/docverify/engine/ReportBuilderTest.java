package com.docverify.engine;

import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.IDCardDocument;
import com.docverify.rules.RequiredFieldRule;
import com.docverify.rules.DateValidityRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReportBuilder — verifies Builder Pattern report generation in HTML and TEXT formats.
 */
class ReportBuilderTest {

    @Test
    @DisplayName("Build HTML report with all fields set")
    void testBuildHtmlReport() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", LocalDate.of(2029, 1, 1));

        VerificationEngine engine = new VerificationEngine();
        engine.addRule(new RequiredFieldRule());
        VerificationReport report = engine.runVerification(doc);

        String html = new ReportBuilder()
                .setTitle("Test Certificate")
                .setDocument(doc)
                .setReport(report)
                .includeDetails(true)
                .setFormat("HTML")
                .build();

        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("Test Certificate"));
        assertTrue(html.contains("DOC-001"));
        assertTrue(html.contains("Test User"));
        assertTrue(html.contains("VERIFIED"));
        assertTrue(html.contains("Required Field Check"));
    }

    @Test
    @DisplayName("Build TEXT report")
    void testBuildTextReport() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-002", "Jane Doe",
                LocalDate.of(2024, 6, 15), "ID-2024-005678", null);

        VerificationEngine engine = new VerificationEngine();
        engine.addRule(new RequiredFieldRule());
        engine.addRule(new DateValidityRule());
        VerificationReport report = engine.runVerification(doc);

        String text = new ReportBuilder()
                .setDocument(doc)
                .setReport(report)
                .setFormat("TEXT")
                .build();

        assertTrue(text.contains("Jane Doe"));
        assertTrue(text.contains("DOC-002"));
        assertTrue(text.contains("VERIFIED"));
        assertTrue(text.contains("RULE DETAILS"));
    }

    @Test
    @DisplayName("Build without details excludes rule table")
    void testBuildWithoutDetails() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-003", "Test",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationEngine engine = new VerificationEngine();
        engine.addRule(new RequiredFieldRule());
        VerificationReport report = engine.runVerification(doc);

        String text = new ReportBuilder()
                .setDocument(doc)
                .setReport(report)
                .includeDetails(false)
                .setFormat("TEXT")
                .build();

        assertFalse(text.contains("RULE DETAILS"));
    }

    @Test
    @DisplayName("Export to file creates the file")
    void testExportToFile() throws Exception {
        Path outputPath = Path.of("target", "test_report.html");
        Files.deleteIfExists(outputPath);

        IDCardDocument doc = new IDCardDocument("DOC-004", "Export Test",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationEngine engine = new VerificationEngine();
        engine.addRule(new RequiredFieldRule());
        VerificationReport report = engine.runVerification(doc);

        new ReportBuilder()
                .setTitle("Export Test Report")
                .setDocument(doc)
                .setReport(report)
                .setFormat("HTML")
                .exportToFile(outputPath);

        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath);
        assertTrue(content.contains("Export Test Report"));

        // Cleanup
        Files.deleteIfExists(outputPath);
    }

    @Test
    @DisplayName("Build without document/report throws IllegalStateException")
    void testBuildWithoutRequiredFieldsThrows() {
        ReportBuilder builder = new ReportBuilder().setTitle("Incomplete");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("Method chaining returns same builder instance")
    void testMethodChaining() {
        ReportBuilder builder = new ReportBuilder();

        ReportBuilder result = builder
                .setTitle("Test")
                .setFormat("HTML")
                .includeDetails(true);

        assertSame(builder, result);
    }
}
