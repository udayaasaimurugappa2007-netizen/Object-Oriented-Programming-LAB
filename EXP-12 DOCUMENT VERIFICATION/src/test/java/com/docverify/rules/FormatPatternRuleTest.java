package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.CertificateDocument;
import com.docverify.model.IDCardDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FormatPatternRule — verifies regex-based format validation.
 */
class FormatPatternRuleTest {

    private FormatPatternRule rule;

    @BeforeEach
    void setUp() {
        rule = new FormatPatternRule();
    }

    @Test
    @DisplayName("PASS: Valid ID number format (ID-YYYY-NNNN)")
    void testPassWithValidIdFormat() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("FAIL: Invalid ID number format")
    void testFailWithInvalidIdFormat() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-002", "Test User",
                LocalDate.of(2024, 1, 1), "INVALID-FORMAT", null);

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("PASS: Valid certificate number format (CERT-YYYY-XXX-NNN)")
    void testPassWithValidCertFormat() throws Exception {
        CertificateDocument doc = new CertificateDocument("DOC-003", "Test User",
                LocalDate.of(2023, 6, 1), "CERT-2023-CS-001", "Board of Education", "A");

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("FAIL: Invalid certificate number format")
    void testFailWithInvalidCertFormat() throws Exception {
        CertificateDocument doc = new CertificateDocument("DOC-004", "Test User",
                LocalDate.of(2023, 6, 1), "BAD-CERT-NUMBER", "Board of Education", "A");

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("PASS: ID number with maximum digits (ID-YYYY-NNNNNNNN)")
    void testPassWithMaxDigitIdFormat() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-005", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-12345678", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
    }
}
