package com.docverify.engine;

/**
 * Represents the result of a single verification rule check.
 * 
 * OOP Concept: ENCAPSULATION
 * All fields are private and final; access only through getters.
 * The object is immutable once created.
 */
public class VerificationResult {

    /**
     * Severity level of a verification failure.
     */
    public enum Severity {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");

        private final String displayName;

        Severity(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String ruleName;
    private final boolean passed;
    private final String message;
    private final Severity severity;

    /**
     * Constructs a new VerificationResult.
     *
     * @param ruleName name of the rule that produced this result
     * @param passed   whether the rule check passed
     * @param message  descriptive message explaining the result
     * @param severity severity level (only meaningful for failures)
     */
    public VerificationResult(String ruleName, boolean passed, String message, Severity severity) {
        this.ruleName = ruleName;
        this.passed = passed;
        this.message = message;
        this.severity = severity;
    }

    // --- Convenience Factory Methods ---

    /**
     * Creates a passing result.
     *
     * @param ruleName name of the rule
     * @param message  success message
     * @return a new passing VerificationResult
     */
    public static VerificationResult pass(String ruleName, String message) {
        return new VerificationResult(ruleName, true, message, Severity.LOW);
    }

    /**
     * Creates a failing result.
     *
     * @param ruleName name of the rule
     * @param message  failure message
     * @param severity severity of the failure
     * @return a new failing VerificationResult
     */
    public static VerificationResult fail(String ruleName, String message, Severity severity) {
        return new VerificationResult(ruleName, false, message, severity);
    }

    // --- Getters ---

    public String getRuleName() {
        return ruleName;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        String status = passed ? "✓ PASS" : "✗ FAIL";
        String severityStr = passed ? "" : " [" + severity.getDisplayName() + "]";
        return String.format("  %s | %-25s | %s%s", status, ruleName, message, severityStr);
    }
}
