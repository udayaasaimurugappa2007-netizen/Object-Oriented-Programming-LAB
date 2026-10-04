package com.docverify.engine;

import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.IDCardDocument;
import com.docverify.rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VerificationEngine — verifies polymorphic rule execution and verdict aggregation.
 */
class VerificationEngineTest {

    private VerificationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new VerificationEngine();
    }

    @Test
    @DisplayName("All rules pass → VERIFIED")
    void testVerifiedWhenAllRulesPass() throws Exception {
        engine.addRule(new RequiredFieldRule());
        engine.addRule(new DateValidityRule());

        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", LocalDate.of(2029, 1, 1));

        VerificationReport report = engine.runVerification(doc);

        assertEquals(VerificationReport.Verdict.VERIFIED, report.getVerdict());
        assertEquals(2, report.getPassedCount());
        assertEquals(0, report.getFailedCount());
    }

    @Test
    @DisplayName("1 rule fails → SUSPICIOUS")
    void testSuspiciousWhenOneRuleFails() throws Exception {
        engine.addRule(new RequiredFieldRule());   // will pass
        engine.addRule(new DateValidityRule());    // will pass
        engine.addRule(new FormatPatternRule());   // will fail (bad format)

        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "INVALID-FORMAT", null);

        VerificationReport report = engine.runVerification(doc);

        assertEquals(VerificationReport.Verdict.SUSPICIOUS, report.getVerdict());
        assertEquals(1, report.getFailedCount());
    }

    @Test
    @DisplayName("2 rules fail → SUSPICIOUS")
    void testSuspiciousWhenTwoRulesFail() throws Exception {
        engine.addRule(new RequiredFieldRule());      // will pass
        engine.addRule(new FormatPatternRule());      // will fail
        engine.addRule(new ChecksumIntegrityRule());  // will fail (no checksum set)

        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "INVALID-FORMAT", null);
        // No checksum set

        VerificationReport report = engine.runVerification(doc);

        assertEquals(VerificationReport.Verdict.SUSPICIOUS, report.getVerdict());
        assertEquals(2, report.getFailedCount());
    }

    @Test
    @DisplayName("3+ rules fail → REJECTED")
    void testRejectedWhenThreeOrMoreRulesFail() throws Exception {
        engine.addRule(new RequiredFieldRule());      // will pass
        engine.addRule(new FormatPatternRule());      // will fail (bad format)
        engine.addRule(new ChecksumIntegrityRule());  // will fail (no checksum)
        engine.addRule(new ExpiryRule());             // will fail (expired)

        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2018, 1, 1), "INVALID-FORMAT",
                LocalDate.of(2020, 1, 1)); // expired
        // No checksum set

        VerificationReport report = engine.runVerification(doc);

        assertEquals(VerificationReport.Verdict.REJECTED, report.getVerdict());
        assertTrue(report.getFailedCount() >= 3);
    }

    @Test
    @DisplayName("Engine with no rules → VERIFIED (vacuously true)")
    void testVerifiedWithNoRules() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationReport report = engine.runVerification(doc);

        assertEquals(VerificationReport.Verdict.VERIFIED, report.getVerdict());
        assertEquals(0, report.getResults().size());
    }

    @Test
    @DisplayName("Null document throws IllegalArgumentException")
    void testNullDocumentThrowsException() {
        engine.addRule(new RequiredFieldRule());

        assertThrows(IllegalArgumentException.class, () ->
                engine.runVerification(null));
    }

    @Test
    @DisplayName("Null rule throws IllegalArgumentException")
    void testNullRuleThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.addRule(null));
    }

    @Test
    @DisplayName("getRuleCount returns correct count")
    void testGetRuleCount() {
        assertEquals(0, engine.getRuleCount());
        engine.addRule(new RequiredFieldRule());
        assertEquals(1, engine.getRuleCount());
        engine.addRule(new DateValidityRule());
        assertEquals(2, engine.getRuleCount());
    }

    @Test
    @DisplayName("Report toString is non-empty and contains verdict")
    void testReportToStringContainsVerdict() throws Exception {
        engine.addRule(new RequiredFieldRule());

        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationReport report = engine.runVerification(doc);
        String reportStr = report.toString();

        assertNotNull(reportStr);
        assertTrue(reportStr.contains("VERIFIED"));
        assertTrue(reportStr.contains("VERIFICATION REPORT"));
    }
}
