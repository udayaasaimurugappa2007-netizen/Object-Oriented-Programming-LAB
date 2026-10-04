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
 * Tests for ExpiryRule — verifies expiry date checking logic.
 */
class ExpiryRuleTest {

    private ExpiryRule rule;

    @BeforeEach
    void setUp() {
        rule = new ExpiryRule();
    }

    @Test
    @DisplayName("PASS: ID card with future expiry date")
    void testPassWithValidExpiry() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234",
                LocalDate.now().plusYears(5));

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertTrue(result.getMessage().contains("valid until"));
    }

    @Test
    @DisplayName("FAIL: ID card that has already expired")
    void testFailWithExpiredDocument() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-002", "Test User",
                LocalDate.of(2018, 1, 1), "ID-2018-001234",
                LocalDate.of(2023, 1, 1));

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("expired"));
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("FAIL (LOW): ID card expiring within 30 days")
    void testWarningWhenExpiringSoon() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-003", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234",
                LocalDate.now().plusDays(15));

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("expiring soon"));
        assertEquals(VerificationResult.Severity.LOW, result.getSeverity());
    }

    @Test
    @DisplayName("PASS: ID card with no expiry date (non-expiring)")
    void testPassWithNoExpiryDate() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-004", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertTrue(result.getMessage().contains("does not expire"));
    }

    @Test
    @DisplayName("PASS: Certificate (expiry not applicable)")
    void testPassForCertificate() throws Exception {
        CertificateDocument doc = new CertificateDocument("DOC-005", "Test User",
                LocalDate.of(2023, 6, 1), "CERT-2023-CS-001", "Board of Education", "A");

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertTrue(result.getMessage().contains("not applicable"));
    }
}
