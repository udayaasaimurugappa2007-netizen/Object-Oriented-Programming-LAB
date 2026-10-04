# Document Verification System

A **Java OOP Document Verification System** that accepts documents (ID cards, certificates), runs them through a pipeline of validation rules, and produces a verification report with a **VERIFIED / SUSPICIOUS / REJECTED** verdict.

Built as an academic and portfolio project demonstrating all four **OOP pillars** (Encapsulation, Inheritance, Polymorphism, Abstraction) alongside **Interfaces**, **Composition**, **Custom Exceptions**, **Design Patterns** (Strategy, Factory, Builder), **JSON Persistence**, and a **Desktop GUI**.

---

## Features

- **Document Hierarchy** — Abstract `Document` base with `IDCardDocument` and `CertificateDocument` subclasses
- **6 Verification Rules** — Required fields, date validity, format pattern (regex), checksum integrity (SHA-256 tamper detection), duplicate detection, and expiry check
- **Polymorphic Engine** — Runs any mix of rules without `if/instanceof` chains
- **Design Patterns**:
  - **Strategy Pattern** — Verification rules are interchangeable, pluggable strategies
  - **Factory Pattern** — `DocumentFactory` creates documents cleanly from type string and field map
  - **Builder Pattern** — `ReportBuilder` provides method-chaining configuration for HTML and text reports
- **Custom Exceptions** — Domain-specific hierarchy (`MissingFieldException`, `TamperDetectedException`, etc.)
- **JSON Persistence** — `JsonDocumentRepository` saves and loads verification history across application restarts
- **Cross-Session Duplicate Detection** — Automatically detects documents previously verified in prior sessions
- **Desktop GUI (Swing)** — Modern interface with live validation, color-coded verdicts/badges, history table, and HTML report export
- **Interactive CLI** — Console interface with pre-built demo scenarios and menu navigation
- **Comprehensive Test Suite** — 55 JUnit 5 test cases covering domain models, rules, engine, builder, repository, and factory

---

## OOP Concept & Design Pattern Mapping

| Concept | Implementation in Code |
|---|---|
| **Encapsulation** | `Document` fields are `private`; access only via validated getters/setters with defensive copying |
| **Abstraction** | `Document.hasRequiredFields()` is abstract — callers don't know how each subtype checks |
| **Inheritance** | `IDCardDocument` and `CertificateDocument` extend `Document` |
| **Polymorphism** | `VerificationEngine` iterates `List<VerificationRule>` calling `.verify()` — dispatch is runtime dynamic |
| **Interfaces** | `Verifiable`, `Reportable`, `VerificationRule`, `DocumentRepository` define contracts independent of implementation |
| **Composition** | `VerificationReport` *has-a* list of `VerificationResult` (not inheritance) |
| **Custom Exceptions** | `DocumentVerificationException` base with `MissingFieldException`, `InvalidDocumentException`, `TamperDetectedException` |
| **Strategy Pattern** | Pluggable verification rules implementing `VerificationRule` |
| **Factory Pattern** | `DocumentFactory.createDocument()` encapsulates object creation |
| **Builder Pattern** | `ReportBuilder` with fluent method chaining to export HTML/Text reports |
| **Repository Pattern** | `DocumentRepository` contract implemented by `JsonDocumentRepository` |

---

## Project Structure

```
document-verification-system/
├── src/main/java/com/docverify/
│   ├── model/
│   │   ├── Document.java               (abstract base class)
│   │   ├── IDCardDocument.java         (concrete subclass)
│   │   ├── CertificateDocument.java    (concrete subclass)
│   │   ├── Verifiable.java             (interface)
│   │   └── Reportable.java             (interface)
│   ├── rules/
│   │   ├── VerificationRule.java       (strategy interface)
│   │   ├── RequiredFieldRule.java
│   │   ├── DateValidityRule.java
│   │   ├── FormatPatternRule.java
│   │   ├── ChecksumIntegrityRule.java
│   │   ├── DuplicateCheckRule.java
│   │   └── ExpiryRule.java
│   ├── engine/
│   │   ├── VerificationEngine.java     (rule orchestrator)
│   │   ├── VerificationResult.java     (immutable result)
│   │   ├── VerificationReport.java     (composite report)
│   │   └── ReportBuilder.java          (builder pattern for HTML/Text)
│   ├── factory/
│   │   └── DocumentFactory.java        (factory pattern)
│   ├── repository/
│   │   ├── DocumentRepository.java     (repository interface)
│   │   ├── JsonDocumentRepository.java (Gson-based persistence)
│   │   └── VerificationRecord.java     (persistence DTO)
│   ├── exceptions/
│   │   ├── DocumentVerificationException.java
│   │   ├── InvalidDocumentException.java
│   │   ├── MissingFieldException.java
│   │   └── TamperDetectedException.java
│   ├── ui/
│   │   └── VerificationApp.java        (Swing Desktop GUI)
│   └── Main.java                       (CLI entry point)
├── src/test/java/com/docverify/
│   ├── rules/
│   │   ├── RequiredFieldRuleTest.java
│   │   ├── DateValidityRuleTest.java
│   │   ├── FormatPatternRuleTest.java
│   │   ├── ChecksumIntegrityRuleTest.java
│   │   ├── DuplicateCheckRuleTest.java
│   │   └── ExpiryRuleTest.java
│   ├── engine/
│   │   ├── VerificationEngineTest.java
│   │   └── ReportBuilderTest.java
│   ├── factory/
│   │   └── DocumentFactoryTest.java
│   └── repository/
│       └── JsonDocumentRepositoryTest.java
├── pom.xml
└── README.md
```

---

## Prerequisites

- **Java 17+** (JDK 17 or JDK 21 recommended)
- **Maven 3.8+**

---

## How to Build & Run

### 1. Compile the Project
```bash
mvn compile
```

### 2. Run All Unit Tests (55 tests)
```bash
mvn test
```

### 3. Launch the Desktop GUI (Swing)
```bash
# Directly launch the GUI
mvn exec:java -Dexec.mainClass="com.docverify.ui.VerificationApp"

# Or via Main with the --gui flag
mvn exec:java -Dexec.mainClass="com.docverify.Main" -Dexec.args="--gui"
```

### 4. Run the Interactive CLI
```bash
mvn exec:java -Dexec.mainClass="com.docverify.Main"
```

### 5. Package and Run Standalone JAR
```bash
mvn package
java -jar target/document-verification-system-1.0-SNAPSHOT.jar
# Or launch GUI from JAR:
java -jar target/document-verification-system-1.0-SNAPSHOT.jar --gui
```

---

## CLI & Demo Scenarios

When you run the CLI (`Main.java`), you are presented with:
```
+-----------------------------------+
|  MAIN MENU                        |
+-----------------------------------+
|  1. Verify an ID Card              |
|  2. Verify a Certificate           |
|  3. Run Demo Scenarios             |
|  4. View Verification History      |
|  5. Export Last Report (HTML)      |
|  6. Launch Graphical UI (Swing)    |
|  7. Exit                           |
+-----------------------------------+
```

Selecting option **3** runs 6 pre-built demonstration scenarios:

| # | Scenario | Expected Verdict | Reason |
|---|---|---|---|
| 1 | Valid ID Card | **VERIFIED** | All fields valid, checksum matches, future expiry |
| 2 | Valid Certificate | **VERIFIED** | All fields valid, valid cert authority & format |
| 3 | Invalid Format ID | **SUSPICIOUS** | ID format does not match `ID-YYYY-NNNN` |
| 4 | Expired ID Card | **SUSPICIOUS** | Expiry date is in the past |
| 5 | Duplicate Submission | **SUSPICIOUS** | Document ID was already verified in history |
| 6 | Tampered Document | **REJECTED** | Owner name altered after SHA-256 seal |

---

## GUI Overview

The **Desktop GUI** (`VerificationApp.java`) provides:
- **Verification Tab**:
  - Switch dynamically between **ID Card** and **Certificate** forms
  - Auto-fill **Load Sample Data** or **Clear Form**
  - **Verify Document** button executes all rules
  - **Color-Coded Verdict Banner**: Green (`VERIFIED`), Amber (`SUSPICIOUS`), Red (`REJECTED`)
  - **Rule Breakdown**: Visual pass/fail cards with rule names, severity badges, and error messages
  - **Export HTML Report**: Generates a self-contained HTML verification certificate
- **History Tab**:
  - Live table of all previous verifications loaded from `verification_history.json`
  - Color-coded verdict cells and pass/fail counters
  - **Refresh** and **Export Selected Report** options

---

## License

Academic / Portfolio Project. Free to use and extend.
