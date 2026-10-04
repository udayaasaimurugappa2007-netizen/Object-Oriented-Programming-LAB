package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.Document;

/**
 * Verifies that all required fields for the document type are present and non-empty.
 *
 * Strategy Pattern: One of many interchangeable verification algorithms.
 * Delegates to the document's own hasRequiredFields() method — which is itself
 * an example of polymorphism (each subclass defines what "required" means).
 */
public class RequiredFieldRule implements VerificationRule {

    @Override
    public VerificationResult verify(Document document) {
        if (document.hasRequiredFields()) {
            return VerificationResult.pass(getRuleName(),
                    "All required fields are present.");
        }
        return VerificationResult.fail(getRuleName(),
                "One or more required fields are missing or empty.",
                Severity.CRITICAL);
    }

    @Override
    public String getRuleName() {
        return "Required Field Check";
    }
}
