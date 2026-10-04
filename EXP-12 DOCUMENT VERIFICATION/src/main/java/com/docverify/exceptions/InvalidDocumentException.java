package com.docverify.exceptions;

/**
 * Thrown when document data is structurally invalid.
 * Examples: unknown document type, unparseable date fields, invalid field combinations.
 */
public class InvalidDocumentException extends DocumentVerificationException {

    public InvalidDocumentException(String message) {
        super(message);
    }

    public InvalidDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
