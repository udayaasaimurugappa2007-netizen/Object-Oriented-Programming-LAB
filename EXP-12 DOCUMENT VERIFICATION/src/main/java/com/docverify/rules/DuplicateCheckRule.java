package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.engine.VerificationResult.Severity;
import com.docverify.model.Document;

import java.util.HashSet;
import java.util.Set;

/**
 * Detects duplicate document submissions by tracking previously verified document IDs.
 * Maintains an in-memory set of verified IDs. If a document with the same ID has
 * already been verified in this session, it is flagged as a duplicate.
 *
 * This can be upgraded to persistent storage in Phase 5 for cross-session detection.
 */
public class DuplicateCheckRule implements VerificationRule {

    private final Set<String> verifiedDocumentIds;

    public DuplicateCheckRule() {
        this.verifiedDocumentIds = new HashSet<>();
    }

    /**
     * Constructor that pre-loads previously verified document IDs.
     * Used for cross-session duplicate detection when loading history from persistence.
     *
     * @param previouslyVerifiedIds set of document IDs already verified in prior sessions
     */
    public DuplicateCheckRule(Set<String> previouslyVerifiedIds) {
        this.verifiedDocumentIds = new HashSet<>(previouslyVerifiedIds);
    }

    @Override
    public VerificationResult verify(Document document) {
        String docId = document.getDocumentId();

        if (docId == null || docId.isEmpty()) {
            return VerificationResult.fail(getRuleName(),
                    "Document ID is missing — cannot check for duplicates.",
                    Severity.MEDIUM);
        }

        if (verifiedDocumentIds.contains(docId)) {
            return VerificationResult.fail(getRuleName(),
                    "Duplicate detected — document ID '" + docId + "' has already been verified.",
                    Severity.HIGH);
        }

        // Register this document as verified
        verifiedDocumentIds.add(docId);
        return VerificationResult.pass(getRuleName(),
                "No duplicate found — document ID is unique.");
    }

    /**
     * Removes a document ID from the verified set.
     *
     * @param docId document ID to remove
     * @return true if the ID was present and removed
     */
    public boolean removeDocumentId(String docId) {
        if (docId != null) {
            return verifiedDocumentIds.remove(docId);
        }
        return false;
    }

    /**
     * Clears the history of verified document IDs.
     * Useful for testing or resetting session state.
     */
    public void clearHistory() {
        verifiedDocumentIds.clear();
    }

    /**
     * Returns the number of documents verified so far.
     *
     * @return count of verified document IDs
     */
    public int getVerifiedCount() {
        return verifiedDocumentIds.size();
    }

    @Override
    public String getRuleName() {
        return "Duplicate Check";
    }
}
