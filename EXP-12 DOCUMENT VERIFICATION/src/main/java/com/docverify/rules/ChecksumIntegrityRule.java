package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.Document;

/**
 * Verifies document integrity by comparing the stored checksum against
 * a freshly computed one.
 *
 * How it works:
 * 1. When a document is created via DocumentFactory, a SHA-256 checksum is computed
 *    from its key fields and stored in the document (the document is "sealed").
 * 2. This rule recomputes the checksum from the current field values.
 * 3. If the checksums match, the document hasn't been tampered with.
 * 4. If they don't match, it means a field was modified after sealing — tamper detected!
 */
public class ChecksumIntegrityRule implements VerificationRule {

    @Override
    public VerificationResult verify(Document document) {
        String storedChecksum = document.getChecksum();

        if (storedChecksum == null || storedChecksum.isEmpty()) {
            return VerificationResult.fail(getRuleName(),
                    "No checksum found — document integrity cannot be verified.",
                    Severity.HIGH);
        }

        String computedChecksum = document.computeChecksum();

        if (storedChecksum.equals(computedChecksum)) {
            return VerificationResult.pass(getRuleName(),
                    "Checksum verified — document integrity intact.");
        }

        return VerificationResult.fail(getRuleName(),
                "Checksum mismatch — document may have been tampered with!",
                Severity.CRITICAL);
    }

    @Override
    public String getRuleName() {
        return "Checksum Integrity Check";
    }
}
