package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.Document;
import com.docverify.model.IDCardDocument;

import java.time.LocalDate;

/**
 * Checks whether a document has expired (for document types that have expiry
 * dates).
 * Currently applicable to IDCardDocument. Certificates typically don't expire.
 *
 * Logic:
 * - If the document has no expiry date, it passes (non-expiring document).
 * - If expired, it fails with HIGH severity.
 * - If expiring within 30 days, it fails with LOW severity (warning).
 * - Otherwise, it passes.
 */
public class ExpiryRule implements VerificationRule {

    private static final int EXPIRY_WARNING_DAYS = 30;

    @Override
    public VerificationResult verify(Document document) {
        if (document instanceof IDCardDocument idCard) {
            return checkExpiry(idCard);
        }

        // Non-ID documents don't have an expiry date — rule not applicable
        return VerificationResult.pass(getRuleName(),
                "Expiry check not applicable for this document type.");
    }

    /**
     * Checks the expiry status of an ID card.
     */
    private VerificationResult checkExpiry(IDCardDocument idCard) {
        LocalDate expiryDate = idCard.getExpiryDate();

        if (expiryDate == null) {
            return VerificationResult.pass(getRuleName(),
                    "No expiry date set — document does not expire.");
        }

        if (expiryDate.isBefore(LocalDate.now())) {
            return VerificationResult.fail(getRuleName(),
                    "Document expired on " + expiryDate + ".",
                    Severity.HIGH);
        }

        // Warn if expiring within the warning threshold
        if (expiryDate.isBefore(LocalDate.now().plusDays(EXPIRY_WARNING_DAYS))) {
            return VerificationResult.fail(getRuleName(),
                    "Document expiring soon: " + expiryDate + ".",
                    Severity.LOW);
        }

        return VerificationResult.pass(getRuleName(),
                "Document is valid until " + expiryDate + ".");
    }

    @Override
    public String getRuleName() {
        return "Expiry Check";
    }
}
