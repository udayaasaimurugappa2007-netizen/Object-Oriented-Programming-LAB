package com.docverify.model;

/**
 * Interface that defines the contract for document structural validation.
 * 
 * OOP Concept: INTERFACE-BASED ABSTRACTION
 * Decouples "what a document must support" from "how it does it".
 * Any class implementing this interface guarantees it can answer
 * whether it is structurally valid, without revealing its internals.
 */
public interface Verifiable {

    /**
     * Checks whether the document is structurally valid.
     * Structural validity means all fields are internally consistent
     * (e.g., dates are logical, required fields are present).
     *
     * @return true if the document structure is valid, false otherwise
     */
    boolean isStructurallyValid();
}
