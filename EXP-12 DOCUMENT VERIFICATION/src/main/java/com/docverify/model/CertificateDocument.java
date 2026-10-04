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
 * Represents a Certificate document (e.g., academic certificate, professional certification).
 *
 * OOP Concept: INHERITANCE
 * Extends the abstract Document class, inheriting common fields and adding
 * certificate-specific fields (certificateNumber, issuingAuthority, grade).
 */
public class CertificateDocument extends Document {

    // --- Private fields specific to Certificates ---
    private String certificateNumber;
    private String issuingAuthority;
    private String grade;

    // ========================
    //      CONSTRUCTORS
    // ========================

    public CertificateDocument() {
        super();
    }

    /**
     * Full constructor for Certificate documents.
     *
     * @param documentId        unique identifier for this document
     * @param ownerName         name of the certificate holder
     * @param issueDate         date the certificate was issued
     * @param certificateNumber the certificate number (e.g., "CERT-2023-CS-001")
     * @param issuingAuthority  the organization that issued the certificate
     * @param grade             grade/score (nullable for pass/fail certificates)
     * @throws MissingFieldException if any required field is null or empty
     * @throws InvalidDocumentException if field values violate domain constraints
     */
    public CertificateDocument(String documentId, String ownerName, LocalDate issueDate,
                               String certificateNumber, String issuingAuthority, String grade)
            throws MissingFieldException, InvalidDocumentException {
        super(documentId, ownerName, issueDate);
        setCertificateNumber(certificateNumber);
        setIssuingAuthority(issuingAuthority);
        setGrade(grade);
    }

    // ====================================================
    //  ENCAPSULATED GETTERS & SETTERS (with validation)
    // ====================================================

    public String getCertificateNumber() {
        return certificateNumber;
    }

    /**
     * Sets the certificate number after validating it is non-null and non-empty.
     *
     * @param certificateNumber the certificate number to set
     * @throws MissingFieldException if certificateNumber is null or empty
     */
    public void setCertificateNumber(String certificateNumber) throws MissingFieldException {
        if (certificateNumber == null || certificateNumber.trim().isEmpty()) {
            throw new MissingFieldException("certificateNumber");
        }
        this.certificateNumber = certificateNumber.trim();
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    /**
     * Sets the issuing authority after validating it is non-null and non-empty.
     *
     * @param issuingAuthority the issuing authority to set
     * @throws MissingFieldException if issuingAuthority is null or empty
     */
    public void setIssuingAuthority(String issuingAuthority) throws MissingFieldException {
        if (issuingAuthority == null || issuingAuthority.trim().isEmpty()) {
            throw new MissingFieldException("issuingAuthority");
        }
        this.issuingAuthority = issuingAuthority.trim();
    }

    public String getGrade() {
        return grade;
    }

    /**
     * Sets the grade/distinction. If provided, it cannot be pure whitespace.
     *
     * @param grade grade string or null
     * @throws InvalidDocumentException if grade is an empty blank string
     */
    public void setGrade(String grade) throws InvalidDocumentException {
        if (grade != null && grade.trim().isEmpty()) {
            throw new InvalidDocumentException("Grade cannot be blank if provided.");
        }
        this.grade = grade != null ? grade.trim() : null;
    }

    // ========================
    //  ABSTRACT IMPLEMENTATIONS
    // ========================

    /**
     * Certificates require: documentId, ownerName, issueDate, certificateNumber, issuingAuthority.
     */
    @Override
    public boolean hasRequiredFields() {
        return getDocumentId() != null && !getDocumentId().isEmpty()
                && getOwnerName() != null && !getOwnerName().isEmpty()
                && getIssueDate() != null
                && certificateNumber != null && !certificateNumber.isEmpty()
                && issuingAuthority != null && !issuingAuthority.isEmpty();
    }

    /**
     * A Certificate is structurally valid if all required fields are present
     * and the issue date is not in the future.
     */
    @Override
    public boolean isStructurallyValid() {
        return hasRequiredFields()
                && getIssueDate().isBefore(LocalDate.now().plusDays(1));
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
                    Objects.toString(certificateNumber, ""),
                    Objects.toString(issuingAuthority, ""),
                    Objects.toString(grade, ""));
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    public String getDocumentType() {
        return "Certificate";
    }

    // ========================
    //    OBJECT OVERRIDES
    // ========================

    @Override
    public String toString() {
        return String.format("Certificate [ID=%s, Owner=%s, CertNo=%s, Authority=%s, Grade=%s, Issued=%s]",
                getDocumentId(), getOwnerName(), certificateNumber, issuingAuthority,
                grade != null ? grade : "N/A", getIssueDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CertificateDocument that = (CertificateDocument) o;
        return Objects.equals(certificateNumber, that.certificateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), certificateNumber);
    }
}
