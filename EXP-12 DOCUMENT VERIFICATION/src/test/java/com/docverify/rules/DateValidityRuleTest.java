package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.IDCardDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateValidityRule — verifies date validation logic.
 */
class DateValidityRuleTest {

    private DateValidityRule rule;

    @BeforeEach
    void setUp() {
        rule = new DateValidityRule();
    }

    @Test
    @DisplayName("PASS: Issue date in the past (valid)")
    void testPassWithValidPastDate() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2023, 6, 15), "ID-2023-001234", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertTrue(result.getMessage().contains("valid"));
    }

    @Test
    @DisplayName("PASS: Issue date is today (valid)")
    void testPassWithTodayDate() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-002", "Test User",
                LocalDate.now(), "ID-2024-001234", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("FAIL: Issue date in the future")
    void testFailWithFutureDate() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-003", "Test User",
                LocalDate.now().plusDays(30), "ID-2024-001234", null);

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("future"));
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("FAIL: Issue date before 1900 (unreasonably old)")
    void testFailWithAncientDate() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-004", "Test User",
                LocalDate.of(1850, 1, 1), "ID-1850-001234", null);

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("old"));
        assertEquals(VerificationResult.Severity.MEDIUM, result.getSeverity());
    }
}
