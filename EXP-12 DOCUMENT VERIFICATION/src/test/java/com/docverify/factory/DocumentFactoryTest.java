package com.docverify.factory;

import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.model.CertificateDocument;
import com.docverify.model.Document;
import com.docverify.model.IDCardDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DocumentFactory — verifies correct subclass creation, checksum sealing,
 * and exception handling on bad input.
 */
class DocumentFactoryTest {

    @Test
    @DisplayName("Creates IDCardDocument for type 'IDCARD'")
    void testCreateIDCardDocument() throws InvalidDocumentException {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-001",
                "ownerName", "Test User",
                "issueDate", "2024-01-15",
                "idNumber", "ID-2024-001234",
                "expiryDate", "2029-01-15"
        );

        Document doc = DocumentFactory.createDocument("IDCARD", fields);

        assertInstanceOf(IDCardDocument.class, doc);
        assertEquals("DOC-001", doc.getDocumentId());
        assertEquals("Test User", doc.getOwnerName());
        assertNotNull(doc.getChecksum()); // Checksum should be sealed
    }

    @Test
    @DisplayName("Creates CertificateDocument for type 'CERTIFICATE'")
    void testCreateCertificateDocument() throws InvalidDocumentException {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-002",
                "ownerName", "Test User",
                "issueDate", "2023-06-20",
                "certificateNumber", "CERT-2023-CS-001",
                "issuingAuthority", "Board of Education",
                "grade", "A+"
        );

        Document doc = DocumentFactory.createDocument("CERTIFICATE", fields);

        assertInstanceOf(CertificateDocument.class, doc);
        assertNotNull(doc.getChecksum());
    }

    @Test
    @DisplayName("Accepts case-insensitive type strings")
    void testCaseInsensitiveType() throws InvalidDocumentException {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-003",
                "ownerName", "Test User",
                "issueDate", "2024-01-15",
                "idNumber", "ID-2024-001234"
        );

        Document doc1 = DocumentFactory.createDocument("idcard", fields);
        assertInstanceOf(IDCardDocument.class, doc1);

        // Use different document ID to avoid collision
        Map<String, String> fields2 = new HashMap<>(fields);
        fields2.put("documentId", "DOC-004");
        Document doc2 = DocumentFactory.createDocument("IdCard", fields2);
        assertInstanceOf(IDCardDocument.class, doc2);
    }

    @Test
    @DisplayName("Throws InvalidDocumentException for unknown type")
    void testUnknownTypeThrowsException() {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-005",
                "ownerName", "Test User",
                "issueDate", "2024-01-15"
        );

        InvalidDocumentException ex = assertThrows(InvalidDocumentException.class, () ->
                DocumentFactory.createDocument("PASSPORT", fields));

        assertTrue(ex.getMessage().contains("Unknown document type"));
    }

    @Test
    @DisplayName("Throws InvalidDocumentException for null type")
    void testNullTypeThrowsException() {
        assertThrows(InvalidDocumentException.class, () ->
                DocumentFactory.createDocument(null, Map.of()));
    }

    @Test
    @DisplayName("Throws InvalidDocumentException for missing required field")
    void testMissingRequiredFieldThrowsException() {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-006",
                "ownerName", "Test User"
                // Missing issueDate and idNumber
        );

        assertThrows(InvalidDocumentException.class, () ->
                DocumentFactory.createDocument("IDCARD", fields));
    }

    @Test
    @DisplayName("Throws InvalidDocumentException for invalid date format")
    void testInvalidDateFormatThrowsException() {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-007",
                "ownerName", "Test User",
                "issueDate", "15-01-2024",  // Wrong format, should be YYYY-MM-DD
                "idNumber", "ID-2024-001234"
        );

        InvalidDocumentException ex = assertThrows(InvalidDocumentException.class, () ->
                DocumentFactory.createDocument("IDCARD", fields));

        assertTrue(ex.getMessage().contains("Invalid date format"));
    }

    @Test
    @DisplayName("Checksum is computed and sealed after creation")
    void testChecksumIsSealed() throws InvalidDocumentException {
        Map<String, String> fields = Map.of(
                "documentId", "DOC-008",
                "ownerName", "Test User",
                "issueDate", "2024-01-15",
                "idNumber", "ID-2024-001234"
        );

        Document doc = DocumentFactory.createDocument("IDCARD", fields);

        assertNotNull(doc.getChecksum());
        assertEquals(doc.computeChecksum(), doc.getChecksum()); // Should match
    }
}
