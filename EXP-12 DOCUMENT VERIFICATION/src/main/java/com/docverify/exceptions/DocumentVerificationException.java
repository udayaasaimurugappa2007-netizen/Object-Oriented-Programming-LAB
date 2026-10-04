package com.docverify.exceptions;

/**
 * Base exception for all document verification errors.
 * 
 * OOP Concept: CUSTOM EXCEPTION HIERARCHY
 * Domain-specific exceptions instead of generic ones — shows deliberate
 * error-design thinking. All verification-related exceptions extend this class.
 */
public class DocumentVerificationException extends Exception {

    public DocumentVerificationException(String message) {
        super(message);
    }

    public DocumentVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
