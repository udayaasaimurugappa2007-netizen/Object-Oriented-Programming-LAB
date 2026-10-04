package com.docverify.model;

import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.exceptions.MissingFieldException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Abstract base class for all document types in the verification system.
 *
 * OOP Concepts demonstrated:
 * - ENCAPSULATION: All fields are private; access only through validated getters/setters.
 *   Setters enforce business rules (e.g., reject null names, missing IDs).
 * - ABSTRACTION: hasRequiredFields() and computeChecksum() are abstract — each subtype
 *   defines its own logic. Callers don't know/care how each subtype checks this.
 * - INHERITANCE: IDCardDocument and CertificateDocument extend this class, reusing
 *   common fields (documentId, ownerName, issueDate) and behavior.
 */
public abstract class Document implements Verifiable {

    // --- Private fields (ENCAPSULATION) ---
    private String documentId;
    private String ownerName;
    private LocalDate issueDate;
    private String checksum;  // For integrity verification (tamper detection)

    // ========================
    //      CONSTRUCTORS
    // ========================

    /**
     * Default constructor for subclass use.
     */
    protected Document() {
        // No-arg constructor
    }

    /**
     * Parameterized constructor with input validation.
     *
     * @param documentId unique identifier for the document
     * @param ownerName  name of the document owner
     * @param issueDate  date the document was issued
     * @throws MissingFieldException if any required field is null or empty
     */
    protected Document(String documentId, String ownerName, LocalDate issueDate)
            throws MissingFieldException {
        setDocumentId(documentId);
        setOwnerName(ownerName);
        setIssueDate(issueDate);
    }

    // ====================================================
    //  ENCAPSULATED GETTERS & SETTERS (with validation)
    // ====================================================

    public String getDocumentId() {
        return documentId;
    }

    /**
     * Sets the document ID after validating it is non-null and non-empty.
     *
     * @param documentId the document ID to set
     * @throws MissingFieldException if documentId is null or empty
     */
    public void setDocumentId(String documentId) throws MissingFieldException {
        if (documentId == null || documentId.trim().isEmpty()) {
            throw new MissingFieldException("documentId");
        }
        this.documentId = documentId.trim();
    }

    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Sets the owner name after validating it is non-null and non-empty.
     *
     * @param ownerName the owner name to set
     * @throws MissingFieldException if ownerName is null or empty
     */
    public void setOwnerName(String ownerName) throws MissingFieldException {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new MissingFieldException("ownerName");
        }
        this.ownerName = ownerName.trim();
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    /**
     * Sets the issue date after validating it is non-null.
     *
     * @param issueDate the issue date to set
     * @throws MissingFieldException if issueDate is null
     */
    public void setIssueDate(LocalDate issueDate) throws MissingFieldException {
        if (issueDate == null) {
            throw new MissingFieldException("issueDate");
        }
        this.issueDate = issueDate;
    }

    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum after validating it is not blank.
     *
     * @param checksum hex digest string
     * @throws InvalidDocumentException if checksum is blank
     */
    public void setChecksum(String checksum) throws InvalidDocumentException {
        if (checksum != null && checksum.trim().isEmpty()) {
            throw new InvalidDocumentException("Checksum cannot be blank if specified.");
        }
        this.checksum = checksum != null ? checksum.trim() : null;
    }

    // ========================
    //    ABSTRACT METHODS
    // ========================

    /**
     * Checks whether all required fields for this document type are present and non-empty.
     * Each subclass defines its own set of required fields.
     *
     * OOP Concept: ABSTRACTION — callers invoke this method without knowing
     * how each concrete document type checks its fields.
     *
     * @return true if all required fields are present
     */
    public abstract boolean hasRequiredFields();

    /**
     * Computes a SHA-256 checksum of the document's key fields for integrity verification.
     * Each subclass includes its specific fields in the computation.
     *
     * @return SHA-256 hex string of the key fields
     */
    public abstract String computeChecksum();

    /**
     * Returns the human-readable type name of this document (e.g., "ID Card", "Certificate").
     *
     * @return document type name
     */
    public abstract String getDocumentType();

    // ========================
    //    OBJECT OVERRIDES
    // ========================

    @Override
    public String toString() {
        return String.format("%s [ID=%s, Owner=%s, Issued=%s]",
                getDocumentType(), documentId, ownerName, issueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(documentId, document.documentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }
}
