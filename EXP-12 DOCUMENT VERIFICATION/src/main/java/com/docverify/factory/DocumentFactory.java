package com.docverify.factory;

import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.exceptions.MissingFieldException;
import com.docverify.model.CertificateDocument;
import com.docverify.model.Document;
import com.docverify.model.IDCardDocument;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Factory class for creating Document instances.
 *
 * OOP Concept: FACTORY PATTERN
 * Encapsulates object-creation logic, keeping it out of the UI/Main layer.
 * Given a type string and a field map, it returns the correct Document subclass
 * with its checksum pre-computed (sealed).
 *
 * Supported types:
 * - "IDCARD" / "ID_CARD" / "ID" → IDCardDocument
 * - "CERTIFICATE" / "CERT" → CertificateDocument
 */
public class DocumentFactory {

    /**
     * Creates a Document of the appropriate subclass based on the type string.
     *
     * @param type   document type ("IDCARD" or "CERTIFICATE")
     * @param fields map of field names to their string values
     * @return a fully constructed and checksum-sealed Document subclass instance
     * @throws InvalidDocumentException if the type is unknown or fields are invalid
     */
    public static Document createDocument(String type, Map<String, String> fields)
            throws InvalidDocumentException {
        if (type == null || type.trim().isEmpty()) {
            throw new InvalidDocumentException("Document type cannot be null or empty.");
        }

        try {
            Document doc = switch (type.trim().toUpperCase()) {
                case "IDCARD", "ID_CARD", "ID" -> createIDCardDocument(fields);
                case "CERTIFICATE", "CERT" -> createCertificateDocument(fields);
                default -> throw new InvalidDocumentException(
                        "Unknown document type: '" + type + "'. Supported types: IDCARD, CERTIFICATE.");
            };

            // Seal the document with a checksum after creation
            doc.setChecksum(doc.computeChecksum());
            return doc;

        } catch (MissingFieldException e) {
            throw new InvalidDocumentException("Failed to create document: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an IDCardDocument from a field map.
     * Required fields: documentId, ownerName, issueDate, idNumber
     * Optional fields: expiryDate
     */
    private static IDCardDocument createIDCardDocument(Map<String, String> fields)
            throws MissingFieldException, InvalidDocumentException {
        validateFieldPresence(fields, "documentId", "ownerName", "issueDate", "idNumber");

        try {
            LocalDate issueDate = LocalDate.parse(fields.get("issueDate"));
            LocalDate expiryDate = fields.containsKey("expiryDate")
                    && fields.get("expiryDate") != null
                    && !fields.get("expiryDate").isEmpty()
                    ? LocalDate.parse(fields.get("expiryDate"))
                    : null;

            return new IDCardDocument(
                    fields.get("documentId"),
                    fields.get("ownerName"),
                    issueDate,
                    fields.get("idNumber"),
                    expiryDate
            );
        } catch (DateTimeParseException e) {
            throw new InvalidDocumentException(
                    "Invalid date format. Use ISO format (YYYY-MM-DD): " + e.getMessage());
        }
    }

    /**
     * Creates a CertificateDocument from a field map.
     * Required fields: documentId, ownerName, issueDate, certificateNumber, issuingAuthority
     * Optional fields: grade
     */
    private static CertificateDocument createCertificateDocument(Map<String, String> fields)
            throws MissingFieldException, InvalidDocumentException {
        validateFieldPresence(fields, "documentId", "ownerName", "issueDate",
                "certificateNumber", "issuingAuthority");

        try {
            LocalDate issueDate = LocalDate.parse(fields.get("issueDate"));
            String grade = fields.getOrDefault("grade", null);

            return new CertificateDocument(
                    fields.get("documentId"),
                    fields.get("ownerName"),
                    issueDate,
                    fields.get("certificateNumber"),
                    fields.get("issuingAuthority"),
                    grade
            );
        } catch (DateTimeParseException e) {
            throw new InvalidDocumentException(
                    "Invalid date format. Use ISO format (YYYY-MM-DD): " + e.getMessage());
        }
    }

    /**
     * Validates that all required fields are present and non-empty in the field map.
     *
     * @param fields         the field map to validate
     * @param requiredFields the names of required fields
     * @throws InvalidDocumentException if any required field is missing or empty
     */
    private static void validateFieldPresence(Map<String, String> fields, String... requiredFields)
            throws InvalidDocumentException {
        if (fields == null) {
            throw new InvalidDocumentException("Field map cannot be null.");
        }
        for (String field : requiredFields) {
            if (!fields.containsKey(field) || fields.get(field) == null
                    || fields.get(field).trim().isEmpty()) {
                throw new InvalidDocumentException("Required field missing in input: " + field);
            }
        }
    }
}
