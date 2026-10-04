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
 * Tests for DuplicateCheckRule — verifies duplicate detection across submissions.
 */
class DuplicateCheckRuleTest {

    private DuplicateCheckRule rule;

    @BeforeEach
    void setUp() {
        rule = new DuplicateCheckRule();
    }

    @Test
    @DisplayName("PASS: First submission of a document ID")
    void testPassOnFirstSubmission() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertEquals(1, rule.getVerifiedCount());
    }

    @Test
    @DisplayName("FAIL: Second submission of the same document ID")
    void testFailOnDuplicateSubmission() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        rule.verify(doc); // First submission — passes
        VerificationResult result = rule.verify(doc); // Second submission — should fail

        assertFalse(result.isPassed());
        assertTrue(result.getMessage().contains("Duplicate"));
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("PASS: Different document IDs are not duplicates")
    void testPassWithDifferentIds() throws Exception {
        IDCardDocument doc1 = new IDCardDocument("DOC-001", "User One",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);
        IDCardDocument doc2 = new IDCardDocument("DOC-002", "User Two",
                LocalDate.of(2024, 1, 1), "ID-2024-005678", null);

        VerificationResult result1 = rule.verify(doc1);
        VerificationResult result2 = rule.verify(doc2);

        assertTrue(result1.isPassed());
        assertTrue(result2.isPassed());
        assertEquals(2, rule.getVerifiedCount());
    }

    @Test
    @DisplayName("clearHistory resets duplicate tracking")
    void testClearHistoryResetsState() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);

        rule.verify(doc); // First submission
        assertEquals(1, rule.getVerifiedCount());

        rule.clearHistory(); // Reset
        assertEquals(0, rule.getVerifiedCount());

        VerificationResult result = rule.verify(doc); // Should pass again
        assertTrue(result.isPassed());
    }
}
