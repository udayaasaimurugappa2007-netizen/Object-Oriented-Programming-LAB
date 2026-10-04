package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.Document;

import java.time.LocalDate;

/**
 * Verifies that the document's issue date is reasonable:
 * - Not in the future
 * - Not before the year 1900 (unreasonably old)
 */
public class DateValidityRule implements VerificationRule {

    private static final LocalDate EARLIEST_VALID_DATE = LocalDate.of(1900, 1, 1);

    @Override
    public VerificationResult verify(Document document) {
        LocalDate issueDate = document.getIssueDate();

        if (issueDate == null) {
            return VerificationResult.fail(getRuleName(),
                    "Issue date is missing.", Severity.HIGH);
        }

        if (issueDate.isAfter(LocalDate.now())) {
            return VerificationResult.fail(getRuleName(),
                    "Issue date is in the future: " + issueDate, Severity.HIGH);
        }

        if (issueDate.isBefore(EARLIEST_VALID_DATE)) {
            return VerificationResult.fail(getRuleName(),
                    "Issue date is unreasonably old: " + issueDate, Severity.MEDIUM);
        }

        return VerificationResult.pass(getRuleName(),
                "Issue date is valid: " + issueDate);
    }

    @Override
    public String getRuleName() {
        return "Date Validity Check";
    }
}
