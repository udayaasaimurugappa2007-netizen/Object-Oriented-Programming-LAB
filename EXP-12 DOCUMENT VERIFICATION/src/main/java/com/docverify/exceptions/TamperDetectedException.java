package com.docverify.exceptions;

/**
 * Thrown when checksum verification detects document tampering.
 * Indicates that the document's fields were modified after the integrity
 * checksum was originally computed and sealed.
 */
public class TamperDetectedException extends DocumentVerificationException {

    public TamperDetectedException(String message) {
        super(message);
    }
}
