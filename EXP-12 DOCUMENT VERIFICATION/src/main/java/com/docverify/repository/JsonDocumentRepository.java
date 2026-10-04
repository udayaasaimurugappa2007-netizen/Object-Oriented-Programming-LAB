package com.docverify.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JSON file-based implementation of DocumentRepository.
 * Uses Gson for serialization/deserialization.
 *
 * Verification history is stored as a JSON array of VerificationRecord objects
 * in a single file. Records are appended on each save and the full list is
 * rewritten (acceptable for the scale of this project).
 *
 * OOP Concept: This class IMPLEMENTS the DocumentRepository interface,
 * providing a concrete JSON-based storage mechanism. The engine never
 * references this class directly — it works through the interface.
 */
public class JsonDocumentRepository implements DocumentRepository {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Type RECORD_LIST_TYPE =
            new TypeToken<List<VerificationRecord>>() {}.getType();

    private final Path filePath;
    private final List<VerificationRecord> records;

    /**
     * Creates a repository backed by the specified JSON file.
     * If the file exists, existing records are loaded on construction.
     *
     * @param filePath path to the JSON storage file
     */
    public JsonDocumentRepository(Path filePath) {
        this.filePath = filePath;
        this.records = new ArrayList<>();
        loadFromFile();
    }

    /**
     * Creates a repository with the default file path ("verification_history.json"
     * in the current working directory).
     */
    public JsonDocumentRepository() {
        this(Path.of("verification_history.json"));
    }

    // ========================
    //  INTERFACE IMPLEMENTATION
    // ========================

    @Override
    public void saveRecord(VerificationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Record cannot be null");
        }
        records.add(record);
        saveToFile();
    }

    @Override
    public List<VerificationRecord> loadAllRecords() {
        return new ArrayList<>(records); // Defensive copy
    }

    @Override
    public Set<String> loadVerifiedDocumentIds() {
        return records.stream()
                .map(VerificationRecord::getDocumentId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public int getRecordCount() {
        return records.size();
    }

    @Override
    public boolean deleteRecord(String documentId) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return false;
        }
        boolean removed = records.removeIf(r -> documentId.trim().equalsIgnoreCase(r.getDocumentId()));
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    @Override
    public void clearAllRecords() {
        records.clear();
        saveToFile();
    }

    // ========================
    //     FILE I/O
    // ========================

    /**
     * Loads records from the JSON file into memory.
     * If the file doesn't exist or is empty, starts with an empty list.
     */
    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            return; // No history file yet — start fresh
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            List<VerificationRecord> loaded = GSON.fromJson(reader, RECORD_LIST_TYPE);
            if (loaded != null) {
                records.addAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("[WARNING] Could not load verification history from "
                    + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Saves all records to the JSON file, overwriting any existing content.
     */
    private void saveToFile() {
        try {
            // Ensure parent directories exist
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(records, RECORD_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            System.err.println("[WARNING] Could not save verification history to "
                    + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Returns the file path used by this repository.
     *
     * @return the JSON file path
     */
    public Path getFilePath() {
        return filePath;
    }
}
