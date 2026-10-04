package com.docverify.rules;

import com.docverify.engine.VerificationResult;
import com.docverify.model.Document;

/**
 * Interface defining the contract for all verification rules.
 *
 * OOP Concepts demonstrated:
 * - ABSTRACTION: Hides HOW a rule checks, exposes only THAT it checks.
 * - STRATEGY PATTERN: Each rule encapsulates a specific verification algorithm
 *   behind this uniform interface. Rules are interchangeable and can be added/removed
 *   from the VerificationEngine without modifying the engine code.
 * - POLYMORPHISM: The VerificationEngine holds a List&lt;VerificationRule&gt; of mixed
 *   concrete types and calls verify() on each — the actual method executed depends
 *   on the runtime object type.
 */
public interface VerificationRule {

    /**
     * Verifies the given document against this rule.
     *
     * @param document the document to verify
     * @return a VerificationResult indicating pass/fail with details
     */
    VerificationResult verify(Document document);

    /**
     * Returns the human-readable name of this rule.
     *
     * @return the rule name
     */
    String getRuleName();
}
