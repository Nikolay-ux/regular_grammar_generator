package org.view;

import org.model.GrammarType;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private JTextField alphabetField;
    private JTextField initialChainField;
    private JTextField finalChainField;
    private JSpinner multiplicitySpinner;
    private JComboBox<GrammarType> grammarTypeCombo;
    private JSpinner minLengthSpinner;
    private JSpinner maxLengthSpinner;

    private JButton generateGrammarButton;
    private JButton generateChainsButton;
    private JButton stepByStepButton;
    private JButton exportButton;
    private JButton clearButton;

    private JTextArea grammarRulesArea;
    private JTextArea formalDefinitionArea;
    private JTable chainsTable;
    private DefaultTableModel chainsTableModel;
    private JTextArea stepsArea;
    private JTextArea currentChainArea;

    private JProgressBar progressBar;
    private JLabel statusLabel;

    private JMenuBar menuBar;
    private JMenu helpMenu;
    private JMenuItem authorMenuItem;
    private JMenuItem themeMenuItem;

    public MainFrame() {
        initComponents();
        setupMenu();
        setupLayout();
        setupWindow();
    }

    private void initComponents() {
        alphabetField = new JTextField(30);
        initialChainField = new JTextField(20);
        finalChainField = new JTextField(20);

        multiplicitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        grammarTypeCombo = new JComboBox<>(GrammarType.values());

        minLengthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        maxLengthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));

        generateGrammarButton = new JButton("Построить грамматику");
        generateChainsButton = new JButton("Сгенерировать цепочки");
        stepByStepButton = new JButton("Пошаговый вывод");
        exportButton = new JButton("Экспорт результатов");
        clearButton = new JButton("Очистить всё");

        grammarRulesArea = new JTextArea(15, 40);
        grammarRulesArea.setEditable(false);
        grammarRulesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        formalDefinitionArea = new JTextArea(10, 40);
        formalDefinitionArea.setEditable(false);
        formalDefinitionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        chainsTableModel = new DefaultTableModel(
                new Object[]{"№", "Цепочка", "Длина", "Соответствие правилам"}, 0
        );
        chainsTable = new JTable(chainsTableModel);
        chainsTable.setFont(new Font("Monospaced", Font.PLAIN, 12));

        stepsArea = new JTextArea(10, 40);
        stepsArea.setEditable(false);
        stepsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        currentChainArea = new JTextArea(5, 40);
        currentChainArea.setEditable(false);
        currentChainArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        currentChainArea.setBackground(new Color(240, 240, 240));

        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        statusLabel = new JLabel("Готово");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setupMenu() {
        menuBar = new JMenuBar();

        helpMenu = new JMenu("Справка");
        helpMenu.setMnemonic('С');

        authorMenuItem = new JMenuItem("Автор", new ImageIcon("src/main/resources/author.png"));
        authorMenuItem.setMnemonic('А');
        authorMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift A"));
        authorMenuItem.addActionListener(e -> showAuthorInfo());

        themeMenuItem = new JMenuItem("Тема", new ImageIcon("src/main/resources/theme.png"));
        themeMenuItem.setMnemonic('Т');
        themeMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift T"));
        themeMenuItem.addActionListener(e -> showThemeInfo());

        helpMenu.add(authorMenuItem);
        helpMenu.add(themeMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void showAuthorInfo() {
        String authorInfo = "<html><body style='width: 300px; padding: 10px;'>" +
                "<h2 style='text-align: center; color: #2c3e50;'>Автор</h2>" +
                "<div style='text-align: center;'>" +
                "<p style='font-size: 16px; font-weight: bold; color: #3498db;'>Соколовский Николай</p>" +
                "<p style='font-size: 14px; color: #7f8c8d;'>Студент группы ИП-213</p>" +
                "</div></body></html>";

        JLabel label = new JLabel(authorInfo);
        JOptionPane.showMessageDialog(this, label, "Об авторе",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showThemeInfo() {
        String themeText = "<html><body style='width: 500px; padding: 10px;'>" +
                "<h2 style='text-align: center; color: #2c3e50;'>Тема 1. Построение конструкций, задающих язык</h2>" +
                "<hr>" +
                "<div style='font-size: 12px; line-height: 1.5;'>" +
                "<p><b>Общее описание для всех тем блока:</b></p>" +
                "<p>Написать программу, которая по предложенному описанию языка построит " +
                "регулярную грамматику (ЛЛ или ПЛ – по выбору пользователя), задающую этот " +
                "язык, и позволит сгенерировать с её помощью все цепочки языка в заданном " +
                "диапазоне длин.</p>" +
                "<p>Предусмотреть возможность поэтапного отображения на экране процесса " +
                "генерации цепочек.</p>" +
                "<p><b>Язык задается следующими параметрами:</b></p>" +
                "<ul>" +
                "<li><b>Алфавит</b> - набор допустимых символов</li>" +
                "<li><b>Начальная подцепочка</b> - обязательное начало всех цепочек</li>" +
                "<li><b>Конечная подцепочка</b> - обязательное окончание всех цепочек</li>" +
                "<li><b>Кратность длины</b> - длина всех цепочек должна быть кратна этому числу</li>" +
                "</ul>" +
                "</div></body></html>";

        JLabel label = new JLabel(themeText);
        JOptionPane.showMessageDialog(this, label, "Тема проекта",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = createInputPanel();

        JPanel controlPanel = createControlPanel();

        JTabbedPane resultsTabbedPane = createResultsTabbedPane();

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(statusLabel, BorderLayout.EAST);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputPanel, BorderLayout.CENTER);
        northPanel.add(controlPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(resultsTabbedPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Параметры языка"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Алфавит (символы без пробелов):"), gbc);
        gbc.gridx = 1;
        panel.add(alphabetField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Начальная подцепочка:"), gbc);
        gbc.gridx = 1;
        panel.add(initialChainField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Конечная подцепочка:"), gbc);
        gbc.gridx = 1;
        panel.add(finalChainField, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Кратность длины:"), gbc);
        gbc.gridx = 1;
        panel.add(multiplicitySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Тип грамматики:"), gbc);
        gbc.gridx = 1;
        panel.add(grammarTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Диапазон длин:"), gbc);
        gbc.gridx = 1;
        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rangePanel.add(new JLabel("от"));
        rangePanel.add(minLengthSpinner);
        rangePanel.add(new JLabel("до"));
        rangePanel.add(maxLengthSpinner);
        panel.add(rangePanel, gbc);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(generateGrammarButton);
        panel.add(generateChainsButton);
        panel.add(stepByStepButton);
        panel.add(exportButton);
        panel.add(clearButton);

        return panel;
    }

    private JTabbedPane createResultsTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel grammarPanel = new JPanel(new BorderLayout());
        grammarPanel.add(new JLabel("Правила грамматики:"), BorderLayout.NORTH);
        grammarPanel.add(new JScrollPane(grammarRulesArea), BorderLayout.CENTER);
        tabbedPane.addTab("Правила грамматики", grammarPanel);

        JPanel formalDefPanel = new JPanel(new BorderLayout());
        formalDefPanel.add(new JLabel("Формальное определение грамматики:"), BorderLayout.NORTH);
        formalDefPanel.add(new JScrollPane(formalDefinitionArea), BorderLayout.CENTER);
        tabbedPane.addTab("Формальное определение", formalDefPanel);

        JPanel chainsPanel = new JPanel(new BorderLayout());
        chainsPanel.add(new JLabel("Сгенерированные цепочки:"), BorderLayout.NORTH);
        chainsPanel.add(new JScrollPane(chainsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Цепочки языка", chainsPanel);

        JPanel stepsPanel = new JPanel(new BorderLayout());
        JPanel stepsTopPanel = new JPanel(new BorderLayout());
        stepsTopPanel.add(new JLabel("Текущая цепочка:"), BorderLayout.NORTH);
        stepsTopPanel.add(new JScrollPane(currentChainArea), BorderLayout.CENTER);

        stepsPanel.add(stepsTopPanel, BorderLayout.NORTH);
        stepsPanel.add(new JLabel("Шаги генерации:"), BorderLayout.CENTER);
        stepsPanel.add(new JScrollPane(stepsArea), BorderLayout.SOUTH);
        tabbedPane.addTab("Пошаговая генерация", stepsPanel);

        return tabbedPane;
    }

    private void setupWindow() {
        setTitle("Генератор регулярных грамматик");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/icon.png")));
        } catch (Exception e) {
        }
    }

    public String getAlphabetText() { return alphabetField.getText().trim(); }
    public String getInitialChain() { return initialChainField.getText().trim(); }
    public String getFinalChain() { return finalChainField.getText().trim(); }
    public int getMultiplicity() { return (Integer) multiplicitySpinner.getValue(); }
    public GrammarType getGrammarType() { return (GrammarType) grammarTypeCombo.getSelectedItem(); }
    public int getMinLength() { return (Integer) minLengthSpinner.getValue(); }
    public int getMaxLength() { return (Integer) maxLengthSpinner.getValue(); }

    public void setGrammarRules(String text) { grammarRulesArea.setText(text); }
    public void setFormalDefinition(String text) { formalDefinitionArea.setText(text); }
    public void setCurrentChain(String text) { currentChainArea.setText(text); }
    public void setSteps(String text) { stepsArea.setText(text); }
    public void appendStep(String text) { stepsArea.append(text + "\n"); }
    public void clearSteps() { stepsArea.setText(""); }

    public void clearChainsTable() {
        chainsTableModel.setRowCount(0);
    }

    public void addChainToTable(int index, String chain, boolean valid) {
        chainsTableModel.addRow(new Object[]{
                index,
                chain,
                chain.length(),
                valid ? "✓" : "✗"
        });
    }

    public void showProgress(boolean show) {
        progressBar.setVisible(show);
        progressBar.setIndeterminate(show);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setErrorStatus(String error) {
        statusLabel.setText("Ошибка: " + error);
        statusLabel.setForeground(Color.RED);
    }

    public void setSuccessStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(0, 100, 0));
    }

    public void setGenerateGrammarListener(ActionListener listener) {
        generateGrammarButton.addActionListener(listener);
    }

    public void setGenerateChainsListener(ActionListener listener) {
        generateChainsButton.addActionListener(listener);
    }

    public void setStepByStepListener(ActionListener listener) {
        stepByStepButton.addActionListener(listener);
    }

    public void setExportListener(ActionListener listener) {
        exportButton.addActionListener(listener);
    }

    public void setClearListener(ActionListener listener) {
        clearButton.addActionListener(listener);
    }

    public boolean validateInput() {
        if (getAlphabetText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Алфавит не может быть пустым", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (getMinLength() > getMaxLength()) {
            JOptionPane.showMessageDialog(this,
                    "Минимальная длина не может быть больше максимальной",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}