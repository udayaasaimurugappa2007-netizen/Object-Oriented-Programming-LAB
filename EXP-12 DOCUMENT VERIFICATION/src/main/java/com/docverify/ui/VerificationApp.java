package com.docverify.ui;

import com.docverify.engine.ReportBuilder;
import com.docverify.engine.VerificationEngine;
import com.docverify.engine.VerificationReport;
import com.docverify.engine.VerificationResult;
import com.docverify.exceptions.InvalidDocumentException;
import com.docverify.factory.DocumentFactory;
import com.docverify.model.Document;
import com.docverify.repository.DocumentRepository;
import com.docverify.repository.JsonDocumentRepository;
import com.docverify.repository.VerificationRecord;
import com.docverify.rules.RequiredFieldRule;
import com.docverify.rules.DateValidityRule;
import com.docverify.rules.FormatPatternRule;
import com.docverify.rules.ChecksumIntegrityRule;
import com.docverify.rules.DuplicateCheckRule;
import com.docverify.rules.ExpiryRule;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Swing-based GUI for the Document Verification System (Phase 6).
 *
 * Design principles:
 * - UI code is THIN — it only orchestrates calls to DocumentFactory,
 *   VerificationEngine, and DocumentRepository. No business logic lives here.
 * - Color-coded results: green (PASS), red (FAIL), amber (SUSPICIOUS)
 * - Two tabs: "Verify Document" (form + results) and "Verification History" (table)
 * - Integrates with Phase 5 persistence (JSON history) and HTML export (Builder Pattern)
 */
public class VerificationApp extends JFrame {

    // ========================
    //    COLOR SCHEME
    // ========================
    private static final Color PRIMARY_DARK = new Color(19, 43, 77);
    private static final Color ACCENT = new Color(20, 132, 152);
    private static final Color SUCCESS_GREEN = new Color(35, 136, 99);
    private static final Color WARNING_AMBER = new Color(205, 130, 31);
    private static final Color DANGER_RED = new Color(190, 65, 67);
    private static final Color BG_LIGHT = new Color(238, 243, 246);
    private static final Color CARD_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(27, 43, 56);
    private static final Color TEXT_MUTED = new Color(96, 112, 122);
    private static final Color BORDER_LIGHT = new Color(211, 222, 228);

    // ========================
    //    FONTS
    // ========================
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_VERDICT = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_RULE = new Font("Segoe UI", Font.PLAIN, 12);

    // ========================
    //    BACKEND
    // ========================
    private VerificationEngine engine;
    private DocumentRepository repository;

    // ========================
    //    INPUT FIELDS
    // ========================
    private JComboBox<String> docTypeCombo;
    private JTextField docIdField, ownerField, issueDateField;
    private JTextField idNumberField, expiryDateField;       // ID Card specific
    private JTextField certNumberField, authorityField, gradeField; // Certificate specific
    private JPanel dynamicFieldsPanel;
    private CardLayout dynamicCardLayout;

    // ========================
    //    RESULTS
    // ========================
    private JLabel verdictLabel;
    private JPanel ruleResultsPanel;
    private JLabel summaryLabel;

    // ========================
    //    HISTORY
    // ========================
    private DefaultTableModel historyTableModel;

    // ========================
    //    STATE
    // ========================
    private Document lastDocument;
    private VerificationReport lastReport;

    // ========================
    //    CONSTRUCTOR
    // ========================

    public VerificationApp() {
        initializeBackend();
        setupFrame();
        buildUI();
        loadHistory();
    }

    // ========================
    //    INITIALIZATION
    // ========================

    private void initializeBackend() {
        repository = new JsonDocumentRepository(Path.of("verification_history.json"));
        engine = new VerificationEngine();

        Set<String> previousIds = repository.loadVerifiedDocumentIds();
        engine.addRule(new RequiredFieldRule());
        engine.addRule(new DateValidityRule());
        engine.addRule(new FormatPatternRule());
        engine.addRule(new ChecksumIntegrityRule());
        engine.addRule(new DuplicateCheckRule(previousIds));
        engine.addRule(new ExpiryRule());
    }

    private void setupFrame() {
        setTitle("Document Verification System v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT);
    }

    // ========================
    //    UI CONSTRUCTION
    // ========================

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // Header banner
        add(createHeader(), BorderLayout.NORTH);

        // Tabbed pane: Verify + History
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_LABEL);
        tabbedPane.setBackground(BG_LIGHT);
        tabbedPane.addTab("  Verify Document  ", createVerifyTab());
        tabbedPane.addTab("  Verification History  ", createHistoryTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(PRIMARY_DARK);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Document Verification System");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleBlock.add(titleLabel);
        header.add(titleBlock, BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(1, 2, 20, 0));
        metrics.setOpaque(false);
        metrics.add(createHeaderMetric(String.valueOf(engine.getRuleCount()), "RULES ACTIVE"));
        metrics.add(createHeaderMetric(String.valueOf(repository.getRecordCount()), "REVIEWS SAVED"));
        header.add(metrics, BorderLayout.EAST);

        return header;
    }

    private JPanel createHeaderMetric(String value, String label) {
        JPanel metric = new JPanel();
        metric.setOpaque(false);
        metric.setLayout(new BoxLayout(metric, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(126, 220, 213));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        labelLabel.setForeground(new Color(190, 210, 218));
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metric.add(valueLabel);
        metric.add(Box.createVerticalStrut(2));
        metric.add(labelLabel);
        return metric;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bar.setBackground(new Color(224, 233, 237));
        bar.setBorder(new EmptyBorder(2, 10, 2, 10));
        JLabel label = new JLabel("READY  /  Verification engine online  /  "
                + engine.getRuleCount() + " rules loaded  |  "
                + repository.getRecordCount() + " records in history");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(TEXT_MUTED);
        bar.add(label);
        return bar;
    }

    // ========================
    //    VERIFY TAB
    // ========================

    private JPanel createVerifyTab() {
        JPanel tab = new JPanel(new BorderLayout(12, 12));
        tab.setBackground(BG_LIGHT);
        tab.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = createFormPanel();
        formPanel.setPreferredSize(new Dimension(370, 0));

        JPanel resultsPanel = createResultsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, resultsPanel);
        splitPane.setDividerLocation(390);
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(null);
        tab.add(splitPane, BorderLayout.CENTER);

        return tab;
    }

    // ========================
    //    FORM PANEL (LEFT)
    // ========================

    private JPanel createFormPanel() {
        JPanel card = createCard("Document Details");
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(CARD_WHITE);

        // Document type selector
        docTypeCombo = new JComboBox<>(new String[]{"ID Card", "Certificate"});
        docTypeCombo.setFont(FONT_INPUT);
        docTypeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        docTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                dynamicCardLayout.show(dynamicFieldsPanel, (String) docTypeCombo.getSelectedItem());
            }
        });
        formContent.add(createFieldGroup("Document Type", docTypeCombo));

        // Common fields
        docIdField = createTextField("e.g., DOC-001");
        formContent.add(createFieldGroup("Document ID *", docIdField));

        ownerField = createTextField("e.g., Priya Sharma");
        formContent.add(createFieldGroup("Owner Name *", ownerField));

        issueDateField = createTextField("YYYY-MM-DD");
        formContent.add(createFieldGroup("Issue Date *", issueDateField));

        // Dynamic fields (CardLayout swaps between ID Card / Certificate fields)
        dynamicCardLayout = new CardLayout();
        dynamicFieldsPanel = new JPanel(dynamicCardLayout);
        dynamicFieldsPanel.setBackground(CARD_WHITE);
        dynamicFieldsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // -- ID Card specific fields --
        JPanel idCardFields = new JPanel();
        idCardFields.setLayout(new BoxLayout(idCardFields, BoxLayout.Y_AXIS));
        idCardFields.setBackground(CARD_WHITE);
        idNumberField = createTextField("ID-YYYY-NNNN");
        idCardFields.add(createFieldGroup("ID Number *", idNumberField));
        expiryDateField = createTextField("YYYY-MM-DD (optional)");
        idCardFields.add(createFieldGroup("Expiry Date", expiryDateField));
        dynamicFieldsPanel.add(idCardFields, "ID Card");

        // -- Certificate specific fields --
        JPanel certFields = new JPanel();
        certFields.setLayout(new BoxLayout(certFields, BoxLayout.Y_AXIS));
        certFields.setBackground(CARD_WHITE);
        certNumberField = createTextField("CERT-YYYY-XXX-NNN");
        certFields.add(createFieldGroup("Certificate Number *", certNumberField));
        authorityField = createTextField("e.g., National Board");
        certFields.add(createFieldGroup("Issuing Authority *", authorityField));
        gradeField = createTextField("e.g., A+ (optional)");
        certFields.add(createFieldGroup("Grade", gradeField));
        dynamicFieldsPanel.add(certFields, "Certificate");

        formContent.add(dynamicFieldsPanel);
        formContent.add(Box.createVerticalStrut(12));

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setBackground(CARD_WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton verifyBtn = createButton("Verify", SUCCESS_GREEN);
        verifyBtn.addActionListener(e -> onVerify());
        buttonPanel.add(verifyBtn);

        JButton sampleBtn = createButton("Load sample", ACCENT);
        sampleBtn.addActionListener(e -> loadSampleData());
        buttonPanel.add(sampleBtn);

        JButton clearBtn = createButton("Clear", TEXT_MUTED);
        clearBtn.addActionListener(e -> onClear());
        buttonPanel.add(clearBtn);

        JButton exportBtn = createButton("Export HTML", ACCENT);
        exportBtn.addActionListener(e -> onExportHtml());
        buttonPanel.add(exportBtn);

        formContent.add(buttonPanel);
        card.add(formContent, BorderLayout.CENTER);
        return card;
    }

    // ========================
    //    RESULTS PANEL (RIGHT)
    // ========================

    private JPanel createResultsPanel() {
        JPanel card = createCard("Verification Results");

        JPanel resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setBackground(CARD_WHITE);

        // Verdict label
        verdictLabel = new JLabel("Ready to verify");
        verdictLabel.setFont(FONT_VERDICT);
        verdictLabel.setForeground(TEXT_MUTED);
        verdictLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        verdictLabel.setBorder(new EmptyBorder(20, 0, 8, 0));
        resultsContainer.add(verdictLabel);

        // Summary label
        summaryLabel = new JLabel("Enter document details or load a sample to begin");
        summaryLabel.setFont(FONT_SUBTITLE);
        summaryLabel.setForeground(TEXT_MUTED);
        summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsContainer.add(summaryLabel);
        resultsContainer.add(Box.createVerticalStrut(15));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        resultsContainer.add(sep);
        resultsContainer.add(Box.createVerticalStrut(10));

        // Rule results (scrollable list)
        ruleResultsPanel = new JPanel();
        ruleResultsPanel.setLayout(new BoxLayout(ruleResultsPanel, BoxLayout.Y_AXIS));
        ruleResultsPanel.setBackground(CARD_WHITE);

        JScrollPane scrollPane = new JScrollPane(ruleResultsPanel);
        scrollPane.setBorder(null);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        resultsContainer.add(scrollPane);

        card.add(resultsContainer, BorderLayout.CENTER);
        return card;
    }

    // ========================
    //    HISTORY TAB
    // ========================

    private JPanel createHistoryTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(BG_LIGHT);
        tab.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel card = createCard("Verification History");

        String[] columns = {"#", "Timestamp", "Type", "Document ID", "Owner", "Verdict", "Passed", "Failed"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(historyTableModel);
        table.setFont(FONT_RULE);
        table.setRowHeight(28);
        table.getTableHeader().setFont(FONT_LABEL);
        table.getTableHeader().setBackground(BG_LIGHT);
        table.setSelectionBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));

        // Verdict column color renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String verdict = value != null ? value.toString() : "";
                if (verdict.contains("VERIFIED")) {
                    c.setForeground(SUCCESS_GREEN);
                } else if (verdict.contains("SUSPICIOUS")) {
                    c.setForeground(WARNING_AMBER);
                } else if (verdict.contains("REJECTED")) {
                    c.setForeground(DANGER_RED);
                } else {
                    c.setForeground(TEXT_DARK);
                }
                setFont(FONT_LABEL);
                return c;
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(55);
        table.getColumnModel().getColumn(7).setPreferredWidth(55);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER_LIGHT));
        card.add(scrollPane, BorderLayout.CENTER);

        // Refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_WHITE);
        JButton refreshBtn = createButton("Refresh", ACCENT);
        refreshBtn.addActionListener(e -> loadHistory());
        buttonPanel.add(refreshBtn);
        card.add(buttonPanel, BorderLayout.SOUTH);

        tab.add(card, BorderLayout.CENTER);
        return tab;
    }

    // ========================
    //    ACTION HANDLERS
    // ========================

    /**
     * Handles the "Verify" button click.
     * Reads form fields, creates a Document via Factory, runs verification,
     * and displays results with color coding.
     */
    private void onVerify() {
        String type = "ID Card".equals(docTypeCombo.getSelectedItem()) ? "IDCARD" : "CERTIFICATE";
        Map<String, String> fields = new HashMap<>();

        fields.put("documentId", getFieldValue(docIdField));
        fields.put("ownerName", getFieldValue(ownerField));
        fields.put("issueDate", getFieldValue(issueDateField));

        if ("IDCARD".equals(type)) {
            fields.put("idNumber", getFieldValue(idNumberField));
            String expiry = getFieldValue(expiryDateField);
            if (!expiry.isEmpty()) {
                fields.put("expiryDate", expiry);
            }
        } else {
            fields.put("certificateNumber", getFieldValue(certNumberField));
            fields.put("issuingAuthority", getFieldValue(authorityField));
            String grade = getFieldValue(gradeField);
            if (!grade.isEmpty()) {
                fields.put("grade", grade);
            }
        }

        try {
            Document doc = DocumentFactory.createDocument(type, fields);
            VerificationReport report = engine.runVerification(doc);

            lastDocument = doc;
            lastReport = report;

            displayResults(report);
            saveToRepository(doc, report);
            loadHistory();
        } catch (InvalidDocumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating document:\n" + e.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Displays verification results with color-coded pass/fail indicators.
     */
    private void displayResults(VerificationReport report) {
        // Update verdict
        verdictLabel.setText(report.getVerdict().getDisplayName());
        Color verdictColor = switch (report.getVerdict()) {
            case VERIFIED -> SUCCESS_GREEN;
            case SUSPICIOUS -> WARNING_AMBER;
            case REJECTED -> DANGER_RED;
        };
        verdictLabel.setForeground(verdictColor);

        // Update summary
        summaryLabel.setText(String.format("%d passed, %d failed out of %d rules",
                report.getPassedCount(), report.getFailedCount(), report.getResults().size()));
        summaryLabel.setForeground(TEXT_DARK);

        // Update individual rule results
        ruleResultsPanel.removeAll();
        for (VerificationResult result : report.getResults()) {
            ruleResultsPanel.add(createRuleResultRow(result));
            ruleResultsPanel.add(Box.createVerticalStrut(5));
        }
        ruleResultsPanel.revalidate();
        ruleResultsPanel.repaint();
    }

    /**
     * Creates a single color-coded row for a rule result.
     * Green background for PASS, red background for FAIL.
     */
    private JPanel createRuleResultRow(VerificationResult result) {
        Color bgColor = result.isPassed() ? new Color(232, 245, 233) : new Color(255, 235, 238);
        Color borderColor = result.isPassed() ? new Color(200, 230, 201) : new Color(255, 205, 210);
        Color statusColor = result.isPassed() ? SUCCESS_GREEN : DANGER_RED;

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(bgColor);
        row.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Status icon + rule name
        String icon = result.isPassed() ? "\u2713 " : "\u2717 ";
        JLabel statusLabel = new JLabel(icon + result.getRuleName());
        statusLabel.setFont(FONT_LABEL);
        statusLabel.setForeground(statusColor);
        statusLabel.setPreferredSize(new Dimension(200, 20));
        row.add(statusLabel, BorderLayout.WEST);

        // Message
        JLabel messageLabel = new JLabel(result.getMessage());
        messageLabel.setFont(FONT_RULE);
        messageLabel.setForeground(TEXT_DARK);
        row.add(messageLabel, BorderLayout.CENTER);

        // Severity badge (failures only)
        if (!result.isPassed()) {
            JLabel severityLabel = new JLabel(result.getSeverity().getDisplayName());
            severityLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            severityLabel.setForeground(Color.WHITE);
            severityLabel.setOpaque(true);
            Color sevColor = switch (result.getSeverity()) {
                case CRITICAL -> DANGER_RED;
                case HIGH -> new Color(230, 74, 25);
                case MEDIUM -> WARNING_AMBER;
                case LOW -> new Color(158, 158, 158);
            };
            severityLabel.setBackground(sevColor);
            severityLabel.setBorder(new EmptyBorder(2, 8, 2, 8));
            row.add(severityLabel, BorderLayout.EAST);
        }

        return row;
    }

    /**
     * Clears all form fields and resets the results panel.
     */
    private void onClear() {
        resetField(docIdField, "e.g., DOC-001");
        resetField(ownerField, "e.g., Priya Sharma");
        resetField(issueDateField, "YYYY-MM-DD");
        resetField(idNumberField, "ID-YYYY-NNNN");
        resetField(expiryDateField, "YYYY-MM-DD (optional)");
        resetField(certNumberField, "CERT-YYYY-XXX-NNN");
        resetField(authorityField, "e.g., National Board");
        resetField(gradeField, "e.g., A+ (optional)");

        verdictLabel.setText("Ready to verify");
        verdictLabel.setForeground(TEXT_MUTED);
        summaryLabel.setText("Enter document details or load a sample to begin");
        ruleResultsPanel.removeAll();
        ruleResultsPanel.revalidate();
        ruleResultsPanel.repaint();
        lastDocument = null;
        lastReport = null;
    }

    private void loadSampleData() {
        docTypeCombo.setSelectedItem("ID Card");
        setFieldValue(docIdField, "DOC-SAMPLE-001");
        setFieldValue(ownerField, "Priya Sharma");
        setFieldValue(issueDateField, "2024-03-15");
        setFieldValue(idNumberField, "ID-2024-001234");
        setFieldValue(expiryDateField, "2029-03-15");
        setFieldValue(certNumberField, "CERT-2023-CS-001");
        setFieldValue(authorityField, "National Board of Education");
        setFieldValue(gradeField, "A+");
        docIdField.requestFocusInWindow();
    }

    /**
     * Exports the last verification report as a styled HTML file.
     * Uses the BUILDER PATTERN via ReportBuilder.
     */
    private void onExportHtml() {
        if (lastReport == null || lastDocument == null) {
            JOptionPane.showMessageDialog(this,
                    "No report to export. Verify a document first.",
                    "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export HTML Verification Report");
        chooser.setSelectedFile(new java.io.File(
                "report_" + lastDocument.getDocumentId() + ".html"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new ReportBuilder()
                        .setTitle("Verification Certificate")
                        .setDocument(lastDocument)
                        .setReport(lastReport)
                        .includeDetails(true)
                        .setFormat("HTML")
                        .exportToFile(chooser.getSelectedFile().toPath());

                JOptionPane.showMessageDialog(this,
                        "Report exported successfully!\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export report:\n" + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ========================
    //    PERSISTENCE
    // ========================

    /**
     * Converts a VerificationReport to a VerificationRecord and saves it.
     */
    private void saveToRepository(Document doc, VerificationReport report) {
        List<VerificationRecord.RuleResultRecord> ruleResults = new ArrayList<>();
        for (VerificationResult result : report.getResults()) {
            ruleResults.add(new VerificationRecord.RuleResultRecord(
                    result.getRuleName(),
                    result.isPassed(),
                    result.getMessage(),
                    result.getSeverity().getDisplayName()
            ));
        }

        VerificationRecord record = new VerificationRecord(
                doc.getDocumentId(),
                doc.getDocumentType(),
                doc.getOwnerName(),
                doc.getIssueDate() != null ? doc.getIssueDate().toString() : "",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                report.getVerdict().getDisplayName(),
                report.getResults().size(),
                (int) report.getPassedCount(),
                (int) report.getFailedCount(),
                ruleResults
        );
        repository.saveRecord(record);
    }

    /**
     * Loads verification history from the JSON repository into the history table.
     */
    private void loadHistory() {
        historyTableModel.setRowCount(0);
        List<VerificationRecord> records = repository.loadAllRecords();
        for (int i = 0; i < records.size(); i++) {
            VerificationRecord r = records.get(i);
            historyTableModel.addRow(new Object[]{
                    i + 1,
                    r.getVerificationTimestamp(),
                    r.getDocumentType(),
                    r.getDocumentId(),
                    r.getOwnerName(),
                    r.getVerdict(),
                    r.getPassedRules(),
                    r.getFailedRules()
            });
        }
    }

    // ========================
    //    UI HELPER METHODS
    // ========================

    /**
     * Creates a card panel with a title header and rounded appearance.
     */
    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        if (title != null) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(PRIMARY_DARK);
            titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
            card.add(titleLabel, BorderLayout.NORTH);
        }

        return card;
    }

    /**
     * Creates a label + field pair stacked vertically.
     */
    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setBackground(CARD_WHITE);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(3));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        }
        group.add(field);

        return group;
    }

    /**
     * Creates a styled text field with placeholder text.
     */
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_INPUT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 8, 5, 8)
        ));
        field.setForeground(TEXT_MUTED);
        field.setText(placeholder);

        // Placeholder behavior
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getForeground().equals(TEXT_MUTED)) {
                    field.setText("");
                    field.setForeground(TEXT_DARK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });
        return field;
    }

    /**
     * Gets the actual value of a text field, returning empty string if placeholder is showing.
     */
    private String getFieldValue(JTextField field) {
        if (field.getForeground().equals(TEXT_MUTED)) {
            return ""; // Placeholder is still showing — no user input
        }
        return field.getText().trim();
    }

    /**
     * Resets a field back to its placeholder state.
     */
    private void resetField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(TEXT_MUTED);
    }

    private void setFieldValue(JTextField field, String value) {
        field.setText(value);
        field.setForeground(TEXT_DARK);
    }

    /**
     * Creates a styled button with hover effect.
     */
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(text.length() > 8 ? 125 : 105, 34));

        // Hover effect
        Color hoverBg = bg.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    // ========================
    //    MAIN ENTRY POINT
    // ========================

    /**
     * Launches the GUI application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default L&F
        }

        SwingUtilities.invokeLater(() -> {
            VerificationApp app = new VerificationApp();
            app.setVisible(true);
        });
    }
}
