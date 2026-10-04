package com.docverify.engine;

import com.docverify.model.Document;
import com.docverify.rules.VerificationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * The core verification engine that runs documents through a pipeline of rules.
 *
 * OOP Concept: POLYMORPHISM
 * The engine holds a List&lt;VerificationRule&gt; of mixed concrete types (RequiredFieldRule,
 * DateValidityRule, FormatPatternRule, etc.) and calls .verify() on each WITHOUT knowing
 * their concrete class. The actual method executed depends on the runtime object type.
 * This eliminates the need for if/instanceof chains.
 *
 * Design Pattern: STRATEGY PATTERN
 * Rules are pluggable strategies — they can be added, removed, or reordered at runtime
 * without changing the engine code.
 */
public class VerificationEngine {

    private final List<VerificationRule> rules;

    public VerificationEngine() {
        this.rules = new ArrayList<>();
    }

    /**
     * Constructs a VerificationEngine with an initial list of rules (Constructor Injection).
     *
     * @param rules list of verification rules to execute
     */
    public VerificationEngine(List<VerificationRule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException("Rules list cannot be null");
        }
        this.rules = new ArrayList<>(rules);
    }

    /**
     * Adds a verification rule to the engine's pipeline.
     *
     * @param rule the rule to add
     * @throws IllegalArgumentException if rule is null
     */
    public void addRule(VerificationRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Verification rule cannot be null");
        }
        rules.add(rule);
    }

    /**
     * Removes a verification rule from the pipeline.
     *
     * @param rule the rule to remove
     * @return true if the rule was found and removed
     */
    public boolean removeRule(VerificationRule rule) {
        return rules.remove(rule);
    }

    /**
     * Returns the number of rules currently in the pipeline.
     *
     * @return rule count
     */
    public int getRuleCount() {
        return rules.size();
    }

    /**
     * Runs all verification rules against the given document and produces a report.
     *
     * This method is the POLYMORPHISM showcase:
     * Each rule.verify(doc) call dispatches to the correct concrete implementation
     * at runtime — RequiredFieldRule.verify(), DateValidityRule.verify(), etc.
     * — without the engine ever using instanceof or type-checking.
     *
     * @param document the document to verify
     * @return a VerificationReport containing all results and an overall verdict
     * @throws IllegalArgumentException if document is null
     */
    public VerificationReport runVerification(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        List<VerificationResult> results = new ArrayList<>();

        for (VerificationRule rule : rules) {
            // Polymorphic call — actual method depends on runtime type of 'rule'
            VerificationResult result = rule.verify(document);
            results.add(result);
        }

        return new VerificationReport(document, results);
    }
}
