package com.docverify.engine;

import com.docverify.model.Document;
import com.docverify.model.Reportable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A verification report composed of multiple VerificationResult objects.
 *
 * OOP Concept: COMPOSITION (has-a, not is-a)
 * VerificationReport HAS a List of VerificationResult objects rather than
 * extending anything — correctly models a real "has-a" relationship.
 *
 * Verdict logic:
 * - VERIFIED:   All rules passed (0 failures)
 * - SUSPICIOUS: 1–2 rules failed
 * - REJECTED:   3 or more rules failed
 */
public class VerificationReport implements Reportable {

    /**
     * Overall verification verdict.
     */
    public enum Verdict {
        VERIFIED("VERIFIED"),
        SUSPICIOUS("SUSPICIOUS"),
        REJECTED("REJECTED");

        private final String displayName;

        Verdict(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Document document;
    private final List<VerificationResult> results;
    private final Verdict verdict;
    private final LocalDateTime timestamp;

    /**
     * Constructs a report from a document and its verification results.
     * The verdict is automatically computed from the results.
     *
     * @param document the document that was verified
     * @param results  the list of individual rule results
     */
    public VerificationReport(Document document, List<VerificationResult> results) {
        this.document = document;
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
        this.timestamp = LocalDateTime.now();
        this.verdict = computeVerdict();
    }

    /**
     * Computes the overall verdict based on the number of failed checks.
     * 0 failures → VERIFIED, 1-2 failures → SUSPICIOUS, 3+ failures → REJECTED
     */
    private Verdict computeVerdict() {
        long failedCount = results.stream()
                .filter(r -> !r.isPassed())
                .count();

        if (failedCount == 0) {
            return Verdict.VERIFIED;
        } else if (failedCount <= 2) {
            return Verdict.SUSPICIOUS;
        } else {
            return Verdict.REJECTED;
        }
    }

    // --- Getters ---

    public Document getDocument() {
        return document;
    }

    public List<VerificationResult> getResults() {
        return results;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getPassedCount() {
        return results.stream().filter(VerificationResult::isPassed).count();
    }

    public long getFailedCount() {
        return results.stream().filter(r -> !r.isPassed()).count();
    }

    // --- Reportable interface implementation ---

    @Override
    public String generateReport() {
        return toString();
    }

    @Override
    public String generateSummary() {
        return String.format("%s [%s] — Owner: %s — Verdict: %s (%d/%d passed)",
                document.getDocumentType(), document.getDocumentId(),
                document.getOwnerName(), verdict.getDisplayName(),
                getPassedCount(), results.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String separator = "=".repeat(70);
        String thinSeparator = "-".repeat(70);

        sb.append("\n").append(separator).append("\n");
        sb.append("  DOCUMENT VERIFICATION REPORT\n");
        sb.append(separator).append("\n");
        sb.append("  Document : ").append(document.toString()).append("\n");
        sb.append("  Timestamp: ").append(timestamp.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append(thinSeparator).append("\n");
        sb.append("  RULE RESULTS:\n");
        sb.append(thinSeparator).append("\n");

        for (VerificationResult result : results) {
            sb.append(result.toString()).append("\n");
        }

        sb.append(thinSeparator).append("\n");
        sb.append(String.format("  Summary: %d passed, %d failed out of %d rules\n",
                getPassedCount(), getFailedCount(), results.size()));
        sb.append(separator).append("\n");
        sb.append("  VERDICT: ").append(verdict.getDisplayName()).append("\n");
        sb.append(separator).append("\n");

        return sb.toString();
    }
}
