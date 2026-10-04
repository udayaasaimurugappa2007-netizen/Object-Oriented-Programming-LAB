package com.docverify.repository;

import java.util.List;
import java.util.Set;

/**
 * Interface for document verification persistence.
 *
 * OOP Concept: INTERFACE-BASED ABSTRACTION for the data layer.
 * Decouples "what persistence must support" from "how it stores data".
 * The engine works with this interface — it doesn't know if the backing store
 * is JSON, CSV, SQLite, or MySQL. Swapping implementations requires zero
 * changes to the engine or rule classes.
 */
public interface DocumentRepository {

    /**
     * Saves a verification record to persistent storage.
     *
     * @param record the verification record to save
     */
    void saveRecord(VerificationRecord record);

    /**
     * Loads all previously saved verification records.
     *
     * @return list of all records, or an empty list if none exist
     */
    List<VerificationRecord> loadAllRecords();

    /**
     * Loads all previously verified document IDs.
     * Used to initialize the DuplicateCheckRule with cross-session history.
     *
     * @return set of document IDs, or an empty set if none exist
     */
    Set<String> loadVerifiedDocumentIds();

    /**
     * Returns the total number of records in the repository.
     *
     * @return record count
     */
    int getRecordCount();

    /**
     * Deletes verification records for a specific document ID.
     *
     * @param documentId the document ID to delete
     * @return true if one or more records were deleted
     */
    boolean deleteRecord(String documentId);

    /**
     * Clears all verification records from the repository.
     */
    void clearAllRecords();
}
