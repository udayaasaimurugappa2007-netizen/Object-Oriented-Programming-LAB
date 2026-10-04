package com.docverify.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * A serializable record of a completed document verification.
 * Used for JSON persistence of verification history.
 *
 * This is a plain POJO with String fields (no LocalDate/LocalDateTime)
 * to ensure clean JSON serialization without custom adapters.
 */
public class VerificationRecord {

    private String documentId;
    private String documentType;
    private String ownerName;
    private String issueDate;
    private String verificationTimestamp;
    private String verdict;
    private int totalRules;
    private int passedRules;
    private int failedRules;
    private List<RuleResultRecord> ruleResults;

    // ========================
    //      CONSTRUCTORS
    // ========================

    public VerificationRecord() {
        this.ruleResults = new ArrayList<>();
    }

    public VerificationRecord(String documentId, String documentType, String ownerName,
                              String issueDate, String verificationTimestamp, String verdict,
                              int totalRules, int passedRules, int failedRules,
                              List<RuleResultRecord> ruleResults) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.ownerName = ownerName;
        this.issueDate = issueDate;
        this.verificationTimestamp = verificationTimestamp;
        this.verdict = verdict;
        this.totalRules = totalRules;
        this.passedRules = passedRules;
        this.failedRules = failedRules;
        this.ruleResults = ruleResults != null ? ruleResults : new ArrayList<>();
    }

    // ========================
    //    GETTERS & SETTERS
    // ========================

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getVerificationTimestamp() { return verificationTimestamp; }
    public void setVerificationTimestamp(String verificationTimestamp) { this.verificationTimestamp = verificationTimestamp; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public int getTotalRules() { return totalRules; }
    public void setTotalRules(int totalRules) { this.totalRules = totalRules; }

    public int getPassedRules() { return passedRules; }
    public void setPassedRules(int passedRules) { this.passedRules = passedRules; }

    public int getFailedRules() { return failedRules; }
    public void setFailedRules(int failedRules) { this.failedRules = failedRules; }

    public List<RuleResultRecord> getRuleResults() { return ruleResults; }
    public void setRuleResults(List<RuleResultRecord> ruleResults) { this.ruleResults = ruleResults; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Owner: %s | Verdict: %s (%d/%d passed)",
                verificationTimestamp, documentType, documentId, ownerName,
                verdict, passedRules, totalRules);
    }

    // ========================
    //    NESTED CLASS
    // ========================

    /**
     * A serializable record of a single rule result within a verification.
     */
    public static class RuleResultRecord {

        private String ruleName;
        private boolean passed;
        private String message;
        private String severity;

        public RuleResultRecord() {}

        public RuleResultRecord(String ruleName, boolean passed, String message, String severity) {
            this.ruleName = ruleName;
            this.passed = passed;
            this.message = message;
            this.severity = severity;
        }

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
