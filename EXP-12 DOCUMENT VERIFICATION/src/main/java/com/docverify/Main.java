package com.docverify;

import com.docverify.engine.VerificationEngine;
import com.docverify.engine.VerificationReport;
import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.factory.DocumentFactory;
import com.docverify.model.Document;
import com.docverify.rules.ChecksumIntegrityRule;
import com.docverify.rules.DateValidityRule;
import com.docverify.rules.DuplicateCheckRule;
import com.docverify.rules.ExpiryRule;
import com.docverify.rules.FormatPatternRule;
import com.docverify.rules.RequiredFieldRule;
import com.docverify.rules.VerificationRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 3 Driver: End-to-End Console Verification Pipeline.
 *
 * Demonstrates:
 * 1. Factory Pattern: DocumentFactory is exclusively used to instantiate Document subclasses.
 * 2. Strategy Pattern: Rules implement VerificationRule independently.
 * 3. Polymorphism: VerificationEngine invokes .verify() over a List of VerificationRule.
 * 4. Composition: VerificationReport is composed of VerificationResult items.
 * 5. Verdict aggregation producing all three possible verdicts:
 *    - VERIFIED
 *    - SUSPICIOUS
 *    - REJECTED
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  DOCUMENT VERIFICATION SYSTEM — PHASE 3 PIPELINE DEMO           ");
        System.out.println("  Factory Pattern -> Verification Engine -> Composite Report     ");
        System.out.println("==================================================================\n");

        // 1. Initialize Verification Engine with rules via Constructor Injection (Constraint #9)
        List<VerificationRule> rules = List.of(
                new RequiredFieldRule(),
                new DateValidityRule(),
                new FormatPatternRule(),
                new ChecksumIntegrityRule(),
                new DuplicateCheckRule(),
                new ExpiryRule()
        );
        VerificationEngine engine = new VerificationEngine(rules);

        System.out.println("[*] VerificationEngine initialized with " + engine.getRuleCount() + " rules.\n");

        try {
            // ==================================================================
            // SCENARIO 1: Valid ID Card -> Expected Verdict: VERIFIED (0 failures)
            // ==================================================================
            System.out.println(">>> RUNNING SCENARIO 1: Valid ID Card Document");
            Map<String, String> validIdData = Map.of(
                    "documentId", "DOC-ID-2024-001",
                    "ownerName", "Rajesh Sharma",
                    "issueDate", "2023-01-15",
                    "idNumber", "ID-2023-456789",
                    "expiryDate", "2028-01-15"
            );
            // Factory Pattern exclusively creates document (Constraint #7)
            Document validDoc = DocumentFactory.createDocument("IDCARD", validIdData);
            VerificationReport report1 = engine.runVerification(validDoc);
            System.out.println(report1);

            // ==================================================================
            // SCENARIO 2: Expired ID Card -> Expected Verdict: SUSPICIOUS (1 failure)
            // ==================================================================
            System.out.println(">>> RUNNING SCENARIO 2: Expired ID Card Document");
            Map<String, String> expiredIdData = Map.of(
                    "documentId", "DOC-ID-2018-099",
                    "ownerName", "Priya Patel",
                    "issueDate", "2018-06-10",
                    "idNumber", "ID-2018-123456",
                    "expiryDate", "2022-06-10" // Expired
            );
            Document expiredDoc = DocumentFactory.createDocument("IDCARD", expiredIdData);
            VerificationReport report2 = engine.runVerification(expiredDoc);
            System.out.println(report2);

            // ==================================================================
            // SCENARIO 3: Tampered Document -> Expected Verdict: REJECTED (3+ failures)
            // ==================================================================
            System.out.println(">>> RUNNING SCENARIO 3: Tampered / Invalid ID Document");
            Map<String, String> flawedData = new HashMap<>(Map.of(
                    "documentId", "DOC-ID-2024-001", // Duplicate ID from Scenario 1!
                    "ownerName", "Vikram Singh",
                    "issueDate", "2020-05-01",
                    "idNumber", "INVALID_FORMAT_123", // Bad format pattern
                    "expiryDate", "2021-05-01"        // Expired
            ));
            Document flawedDoc = DocumentFactory.createDocument("IDCARD", flawedData);
            // Simulate post-issuance tampering to trigger ChecksumIntegrity failure:
            flawedDoc.setOwnerName("FORGED IDENTITY OWNER");

            VerificationReport report3 = engine.runVerification(flawedDoc);
            System.out.println(report3);

            // Verdict distribution check
            System.out.println("==================================================================");
            System.out.println("  PIPELINE VERDICT SUMMARY ACROSS RUNS:                           ");
            System.out.println("  Scenario 1: " + report1.getVerdict());
            System.out.println("  Scenario 2: " + report2.getVerdict());
            System.out.println("  Scenario 3: " + report3.getVerdict());
            System.out.println("==================================================================");

        } catch (InvalidDocumentException e) {
            System.err.println("Document creation failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected pipeline error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
