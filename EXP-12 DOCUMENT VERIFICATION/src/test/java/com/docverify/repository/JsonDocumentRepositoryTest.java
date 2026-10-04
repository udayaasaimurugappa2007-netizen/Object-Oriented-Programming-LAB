package com.docverify.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JsonDocumentRepository — verifies JSON persistence, cross-session loading,
 * and duplicate ID extraction.
 */
class JsonDocumentRepositoryTest {

    private static final Path TEST_FILE = Path.of("target", "test_verification_history.json");
    private JsonDocumentRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        // Ensure clean state
        Files.deleteIfExists(TEST_FILE);
        repository = new JsonDocumentRepository(TEST_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEST_FILE);
    }

    @Test
    @DisplayName("Save and load a single record")
    void testSaveAndLoadSingleRecord() {
        VerificationRecord record = createSampleRecord("DOC-001", "VERIFIED");

        repository.saveRecord(record);

        List<VerificationRecord> loaded = repository.loadAllRecords();
        assertEquals(1, loaded.size());
        assertEquals("DOC-001", loaded.get(0).getDocumentId());
        assertEquals("VERIFIED", loaded.get(0).getVerdict());
    }

    @Test
    @DisplayName("Save multiple records and verify count")
    void testSaveMultipleRecords() {
        repository.saveRecord(createSampleRecord("DOC-001", "VERIFIED"));
        repository.saveRecord(createSampleRecord("DOC-002", "SUSPICIOUS"));
        repository.saveRecord(createSampleRecord("DOC-003", "REJECTED"));

        assertEquals(3, repository.getRecordCount());
        assertEquals(3, repository.loadAllRecords().size());
    }

    @Test
    @DisplayName("Persistence survives across repository instances")
    void testCrossSessionPersistence() {
        // First session — save records
        repository.saveRecord(createSampleRecord("DOC-001", "VERIFIED"));
        repository.saveRecord(createSampleRecord("DOC-002", "SUSPICIOUS"));

        // Second session — create new repository instance pointing to same file
        JsonDocumentRepository secondSession = new JsonDocumentRepository(TEST_FILE);

        List<VerificationRecord> loaded = secondSession.loadAllRecords();
        assertEquals(2, loaded.size());
        assertEquals("DOC-001", loaded.get(0).getDocumentId());
        assertEquals("DOC-002", loaded.get(1).getDocumentId());
    }

    @Test
    @DisplayName("loadVerifiedDocumentIds returns all previously verified IDs")
    void testLoadVerifiedDocumentIds() {
        repository.saveRecord(createSampleRecord("DOC-001", "VERIFIED"));
        repository.saveRecord(createSampleRecord("DOC-002", "SUSPICIOUS"));
        repository.saveRecord(createSampleRecord("DOC-003", "REJECTED"));

        Set<String> ids = repository.loadVerifiedDocumentIds();

        assertEquals(3, ids.size());
        assertTrue(ids.contains("DOC-001"));
        assertTrue(ids.contains("DOC-002"));
        assertTrue(ids.contains("DOC-003"));
    }

    @Test
    @DisplayName("Empty repository returns empty list and set")
    void testEmptyRepository() {
        assertEquals(0, repository.getRecordCount());
        assertTrue(repository.loadAllRecords().isEmpty());
        assertTrue(repository.loadVerifiedDocumentIds().isEmpty());
    }

    @Test
    @DisplayName("JSON file is created after first save")
    void testFileCreatedOnSave() {
        assertFalse(Files.exists(TEST_FILE));

        repository.saveRecord(createSampleRecord("DOC-001", "VERIFIED"));

        assertTrue(Files.exists(TEST_FILE));
    }

    @Test
    @DisplayName("Rule results are persisted correctly")
    void testRuleResultsPersisted() {
        VerificationRecord record = createSampleRecord("DOC-001", "VERIFIED");
        record.setRuleResults(List.of(
                new VerificationRecord.RuleResultRecord("Required Field Check", true, "All fields present", "Low"),
                new VerificationRecord.RuleResultRecord("Format Check", false, "Invalid format", "High")
        ));

        repository.saveRecord(record);

        // Load from new instance to verify file persistence
        JsonDocumentRepository reloaded = new JsonDocumentRepository(TEST_FILE);
        List<VerificationRecord.RuleResultRecord> results = reloaded.loadAllRecords().get(0).getRuleResults();

        assertEquals(2, results.size());
        assertTrue(results.get(0).isPassed());
        assertFalse(results.get(1).isPassed());
        assertEquals("Format Check", results.get(1).getRuleName());
    }

    // --- Helper ---

    private VerificationRecord createSampleRecord(String docId, String verdict) {
        return new VerificationRecord(
                docId, "ID Card", "Test User", "2024-01-15",
                "2024-01-15 10:30:00", verdict,
                6, verdict.equals("VERIFIED") ? 6 : 4, verdict.equals("VERIFIED") ? 0 : 2,
                List.of()
        );
    }
}
