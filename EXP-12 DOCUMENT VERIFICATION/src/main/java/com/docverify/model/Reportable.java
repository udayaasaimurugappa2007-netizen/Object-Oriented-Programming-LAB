package com.docverify.model;

/**
 * Interface for objects that can produce formatted reports in different formats.
 *
 * OOP Concept: INTERFACE — defines a "reportable" contract independent of implementation.
 * Both Document and VerificationReport can implement this to produce their own
 * formatted output without coupling to a specific format.
 */
public interface Reportable {

    /**
     * Generates a full detailed report as a formatted string.
     *
     * @return the formatted report content
     */
    String generateReport();

    /**
     * Generates a brief one-line summary.
     *
     * @return a concise summary string
     */
    String generateSummary();
}
