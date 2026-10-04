package com.docverify.exceptions;

/**
 * Thrown when a required document field is null or empty.
 * Carries the specific field name for clear, actionable error reporting.
 */
public class MissingFieldException extends DocumentVerificationException {

    private final String fieldName;

    public MissingFieldException(String fieldName) {
        super("Required field is missing: " + fieldName);
        this.fieldName = fieldName;
    }

    /**
     * Returns the name of the missing field.
     *
     * @return the field name that was null or empty
     */
    public String getFieldName() {
        return fieldName;
    }
}
