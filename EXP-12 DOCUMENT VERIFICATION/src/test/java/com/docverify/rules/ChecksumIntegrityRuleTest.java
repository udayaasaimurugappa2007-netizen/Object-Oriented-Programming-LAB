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
 * Tests for ChecksumIntegrityRule — verifies tamper detection via checksum comparison.
 */
class ChecksumIntegrityRuleTest {

    private ChecksumIntegrityRule rule;

    @BeforeEach
    void setUp() {
        rule = new ChecksumIntegrityRule();
    }

    @Test
    @DisplayName("PASS: Checksum matches — document not tampered")
    void testPassWhenChecksumMatches() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Priya Sharma",
                LocalDate.of(2024, 3, 15), "ID-2024-001234", LocalDate.of(2029, 3, 15));
        // Seal the document
        doc.setChecksum(doc.computeChecksum());

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertTrue(result.getMessage().contains("intact"));
    }

    @Test
    @DisplayName("FAIL: Checksum mismatch — document tampered")
    void testFailWhenDocumentTampered() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-002", "Original Name",
                LocalDate.of(2024, 3, 15), "ID-2024-001234", null);
        // Seal the document
        doc.setChecksum(doc.computeChecksum());
        // Tamper with the document after sealing
        doc.setOwnerName("Forged Name");

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertEquals(VerificationResult.Severity.CRITICAL, result.getSeverity());
        assertTrue(result.getMessage().contains("tampered"));
    }

    @Test
    @DisplayName("FAIL: No checksum set — cannot verify integrity")
    void testFailWhenNoChecksum() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-003", "Test User",
                LocalDate.of(2024, 1, 1), "ID-2024-001234", null);
        // Deliberately do NOT set a checksum

        VerificationResult result = rule.verify(doc);

        assertFalse(result.isPassed());
        assertEquals(VerificationResult.Severity.HIGH, result.getSeverity());
    }
}
