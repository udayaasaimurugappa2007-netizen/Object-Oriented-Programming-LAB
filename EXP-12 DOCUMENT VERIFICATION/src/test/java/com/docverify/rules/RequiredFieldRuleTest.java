package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.IDCardDocument;
import com.docverify.model.CertificateDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RequiredFieldRule — verifies pass/fail for documents with and without required fields.
 */
class RequiredFieldRuleTest {

    private RequiredFieldRule rule;

    @BeforeEach
    void setUp() {
        rule = new RequiredFieldRule();
    }

    @Test
    @DisplayName("PASS: ID Card with all required fields")
    void testPassWhenAllFieldsPresent() throws Exception {
        IDCardDocument doc = new IDCardDocument("DOC-001", "Priya Sharma",
                LocalDate.of(2024, 3, 15), "ID-2024-001234", LocalDate.of(2029, 3, 15));

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
        assertEquals("Required Field Check", result.getRuleName());
    }

    @Test
    @DisplayName("PASS: Certificate with all required fields")
    void testPassCertificateWithAllFields() throws Exception {
        CertificateDocument doc = new CertificateDocument("DOC-002", "Rahul Verma",
                LocalDate.of(2023, 6, 20), "CERT-2023-CS-001", "National Board", "A+");

        VerificationResult result = rule.verify(doc);

        assertTrue(result.isPassed());
    }

    @Test
    @DisplayName("FAIL: ID Card missing idNumber throws MissingFieldException during construction")
    void testFailWhenIdNumberMissing() {
        assertThrows(MissingFieldException.class, () ->
                new IDCardDocument("DOC-003", "Test User",
                        LocalDate.of(2024, 1, 1), null, null));
    }

    @Test
    @DisplayName("FAIL: Certificate missing issuingAuthority throws MissingFieldException")
    void testFailWhenIssuingAuthorityMissing() {
        assertThrows(MissingFieldException.class, () ->
                new CertificateDocument("DOC-004", "Test User",
                        LocalDate.of(2024, 1, 1), "CERT-001", null, "A"));
    }
}
