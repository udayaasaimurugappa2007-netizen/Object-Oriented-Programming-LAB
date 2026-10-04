package com.docverify.model;

import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.exceptions.MissingFieldException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Represents an ID Card document.
 *
 * OOP Concept: INHERITANCE
 * Extends the abstract Document class, inheriting common fields (documentId, ownerName,
 * issueDate) and adding ID-card-specific fields (idNumber, expiryDate).
 */
public class IDCardDocument extends Document {

    // --- Private fields specific to ID Cards ---
    private String idNumber;
    private LocalDate expiryDate;

    // ========================
    //      CONSTRUCTORS
    // ========================

    public IDCardDocument() {
        super();
    }

    /**
     * Full constructor for ID Card documents.
     *
     * @param documentId unique identifier for this document
     * @param ownerName  name of the cardholder
     * @param issueDate  date the ID card was issued
     * @param idNumber   the ID card number (e.g., "ID-2024-001234")
     * @param expiryDate when the ID card expires (null if non-expiring)
     * @throws MissingFieldException if any required field is null or empty
     * @throws InvalidDocumentException if field values violate domain constraints
     */
    public IDCardDocument(String documentId, String ownerName, LocalDate issueDate,
                          String idNumber, LocalDate expiryDate)
            throws MissingFieldException, InvalidDocumentException {
        super(documentId, ownerName, issueDate);
        setIdNumber(idNumber);
        setExpiryDate(expiryDate);
    }

    // ====================================================
    //  ENCAPSULATED GETTERS & SETTERS (with validation)
    // ====================================================

    public String getIdNumber() {
        return idNumber;
    }

    /**
     * Sets the ID card number after validating it is non-null and non-empty.
     *
     * @param idNumber the ID number to set
     * @throws MissingFieldException if idNumber is null or empty
     */
    public void setIdNumber(String idNumber) throws MissingFieldException {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            throw new MissingFieldException("idNumber");
        }
        this.idNumber = idNumber.trim();
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiry date after validating it is not chronologically before the issue date.
     *
     * @param expiryDate the expiry date to set (null allowed for non-expiring IDs)
     * @throws InvalidDocumentException if expiry date is before issue date
     */
    public void setExpiryDate(LocalDate expiryDate) throws InvalidDocumentException {
        if (expiryDate != null && getIssueDate() != null && expiryDate.isBefore(getIssueDate())) {
            throw new InvalidDocumentException("Expiry date (" + expiryDate + ") cannot be before issue date (" + getIssueDate() + ").");
        }
        this.expiryDate = expiryDate;
    }

    // ========================
    //  ABSTRACT IMPLEMENTATIONS
    // ========================

    /**
     * ID Cards require: documentId, ownerName, issueDate, and idNumber.
     */
    @Override
    public boolean hasRequiredFields() {
        return getDocumentId() != null && !getDocumentId().isEmpty()
                && getOwnerName() != null && !getOwnerName().isEmpty()
                && getIssueDate() != null
                && idNumber != null && !idNumber.isEmpty();
    }

    /**
     * An ID Card is structurally valid if all required fields are present,
     * the issue date is not in the future, and the expiry date (if set)
     * is after the issue date.
     */
    @Override
    public boolean isStructurallyValid() {
        return hasRequiredFields()
                && getIssueDate().isBefore(LocalDate.now().plusDays(1))
                && (expiryDate == null || expiryDate.isAfter(getIssueDate()));
    }

    /**
     * Computes SHA-256 hash of all key fields for tamper detection.
     */
    @Override
    public String computeChecksum() {
        try {
            String data = String.join("|",
                    Objects.toString(getDocumentId(), ""),
                    Objects.toString(getOwnerName(), ""),
                    Objects.toString(getIssueDate(), ""),
                    Objects.toString(idNumber, ""),
                    Objects.toString(expiryDate, ""));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public String getDocumentType() {
        return "ID Card";
    }

    // ========================
    //    OBJECT OVERRIDES
    // ========================

    @Override
    public String toString() {
        return String.format("ID Card [ID=%s, Owner=%s, IDNumber=%s, Issued=%s, Expires=%s]",
                getDocumentId(), getOwnerName(), idNumber, getIssueDate(),
                expiryDate != null ? expiryDate.toString() : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        IDCardDocument that = (IDCardDocument) o;
        return Objects.equals(idNumber, that.idNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idNumber);
    }
}
