package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.CertificateDocument;
import com.docverify.model.Document;
import com.docverify.model.IDCardDocument;

import java.util.regex.Pattern;

/**
 * Verifies that ID numbers and certificate numbers match expected format patterns.
 * Uses regex-based validation.
 *
 * Expected formats:
 * - ID Card number: "ID-YYYY-NNNN" where YYYY is a 4-digit year and NNNN is 4-8 digits
 * - Certificate number: "CERT-YYYY-XXX-NNN" where XXX is 2-5 uppercase letters
 */
public class FormatPatternRule implements VerificationRule {

    // ID number pattern: ID-YYYY-NNNN (4-8 digit sequence)
    private static final Pattern ID_NUMBER_PATTERN =
            Pattern.compile("^ID-\\d{4}-\\d{4,8}$");

    // Certificate number pattern: CERT-YYYY-XXX-NNN (2-5 letter code, 3-6 digit sequence)
    private static final Pattern CERT_NUMBER_PATTERN =
            Pattern.compile("^CERT-\\d{4}-[A-Z]{2,5}-\\d{3,6}$");

    @Override
    public VerificationResult verify(Document document) {
        if (document instanceof IDCardDocument idCard) {
            return verifyIDCardFormat(idCard);
        }

        if (document instanceof CertificateDocument cert) {
            return verifyCertificateFormat(cert);
        }

        return VerificationResult.fail(getRuleName(),
                "Unknown document type — cannot validate format.", Severity.MEDIUM);
    }

    /**
     * Validates ID card number against the expected pattern.
     */
    private VerificationResult verifyIDCardFormat(IDCardDocument idCard) {
        String idNumber = idCard.getIdNumber();
        if (idNumber == null || !ID_NUMBER_PATTERN.matcher(idNumber).matches()) {
            return VerificationResult.fail(getRuleName(),
                    "ID number '" + idNumber + "' does not match expected format (ID-YYYY-NNNN).",
                    Severity.HIGH);
        }
        return VerificationResult.pass(getRuleName(),
                "ID number format is valid.");
    }

    /**
     * Validates certificate number against the expected pattern.
     */
    private VerificationResult verifyCertificateFormat(CertificateDocument cert) {
        String certNumber = cert.getCertificateNumber();
        if (certNumber == null || !CERT_NUMBER_PATTERN.matcher(certNumber).matches()) {
            return VerificationResult.fail(getRuleName(),
                    "Certificate number '" + certNumber
                            + "' does not match expected format (CERT-YYYY-XXX-NNN).",
                    Severity.HIGH);
        }
        return VerificationResult.pass(getRuleName(),
                "Certificate number format is valid.");
    }

    @Override
    public String getRuleName() {
        return "Format Pattern Check";
    }
}
