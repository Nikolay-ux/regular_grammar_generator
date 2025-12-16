package org.view;

import org.model.GrammarType;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    // Компоненты ввода
    private JTextField alphabetField;
    private JTextField initialChainField;
    private JTextField finalChainField;
    private JSpinner multiplicitySpinner;
    private JComboBox<GrammarType> grammarTypeCombo;
    private JSpinner minLengthSpinner;
    private JSpinner maxLengthSpinner;

    // Кнопки
    private JButton generateGrammarButton;
    private JButton generateChainsButton;
    private JButton stepByStepButton;
    private JButton exportButton;
    private JButton clearButton;

    // Области вывода
    private JTextArea grammarRulesArea;
    private JTextArea formalDefinitionArea;
    private JTable chainsTable;
    private DefaultTableModel chainsTableModel;
    private JTextArea stepsArea;
    private JTextArea currentChainArea;

    // Панели
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public MainFrame() {
        initComponents();
        setupLayout();
        setupWindow();
    }

    private void initComponents() {
        // Поля ввода
        alphabetField = new JTextField(30);
        initialChainField = new JTextField(20);
        finalChainField = new JTextField(20);

        multiplicitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        grammarTypeCombo = new JComboBox<>(GrammarType.values());

        minLengthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        maxLengthSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));

        // Кнопки
        generateGrammarButton = new JButton("Построить грамматику");
        generateChainsButton = new JButton("Сгенерировать цепочки");
        stepByStepButton = new JButton("Пошаговый вывод");
        exportButton = new JButton("Экспорт результатов");
        clearButton = new JButton("Очистить всё");

        // Области вывода
        grammarRulesArea = new JTextArea(15, 40);
        grammarRulesArea.setEditable(false);
        grammarRulesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        formalDefinitionArea = new JTextArea(10, 40);
        formalDefinitionArea.setEditable(false);
        formalDefinitionArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Таблица цепочек
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

        // Статусные компоненты
        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        statusLabel = new JLabel("Готово");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Панель ввода параметров
        JPanel inputPanel = createInputPanel();

        // Панель кнопок управления
        JPanel controlPanel = createControlPanel();

        // Панель с результатами (вкладки)
        JTabbedPane resultsTabbedPane = createResultsTabbedPane();

        // Панель статуса
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(progressBar, BorderLayout.CENTER);
        statusPanel.add(statusLabel, BorderLayout.EAST);

        // Сборка главного окна
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

        // Алфавит
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Алфавит (символы без пробелов):"), gbc);
        gbc.gridx = 1;
        panel.add(alphabetField, gbc);

        // Начальная подцепочка
        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Начальная подцепочка:"), gbc);
        gbc.gridx = 1;
        panel.add(initialChainField, gbc);

        // Конечная подцепочка
        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Конечная подцепочка:"), gbc);
        gbc.gridx = 1;
        panel.add(finalChainField, gbc);

        // Кратность длины
        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Кратность длины:"), gbc);
        gbc.gridx = 1;
        panel.add(multiplicitySpinner, gbc);

        // Тип грамматики
        gbc.gridx = 0; gbc.gridy = ++row;
        panel.add(new JLabel("Тип грамматики:"), gbc);
        gbc.gridx = 1;
        panel.add(grammarTypeCombo, gbc);

        // Диапазон длин
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

        // Вкладка с правилами грамматики
        JPanel grammarPanel = new JPanel(new BorderLayout());
        grammarPanel.add(new JLabel("Правила грамматики:"), BorderLayout.NORTH);
        grammarPanel.add(new JScrollPane(grammarRulesArea), BorderLayout.CENTER);
        tabbedPane.addTab("Правила грамматики", grammarPanel);

        // Вкладка с формальным определением
        JPanel formalDefPanel = new JPanel(new BorderLayout());
        formalDefPanel.add(new JLabel("Формальное определение грамматики:"), BorderLayout.NORTH);
        formalDefPanel.add(new JScrollPane(formalDefinitionArea), BorderLayout.CENTER);
        tabbedPane.addTab("Формальное определение", formalDefPanel);

        // Вкладка с цепочками
        JPanel chainsPanel = new JPanel(new BorderLayout());
        chainsPanel.add(new JLabel("Сгенерированные цепочки:"), BorderLayout.NORTH);
        chainsPanel.add(new JScrollPane(chainsTable), BorderLayout.CENTER);
        tabbedPane.addTab("Цепочки языка", chainsPanel);

        // Вкладка с шагами генерации
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

        // Устанавливаем иконку
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Иконка не найдена, это не критично
        }
    }

    // Геттеры для полей ввода
    public String getAlphabetText() { return alphabetField.getText().trim(); }
    public String getInitialChain() { return initialChainField.getText().trim(); }
    public String getFinalChain() { return finalChainField.getText().trim(); }
    public int getMultiplicity() { return (Integer) multiplicitySpinner.getValue(); }
    public GrammarType getGrammarType() { return (GrammarType) grammarTypeCombo.getSelectedItem(); }
    public int getMinLength() { return (Integer) minLengthSpinner.getValue(); }
    public int getMaxLength() { return (Integer) maxLengthSpinner.getValue(); }

    // Сеттеры для вывода
    public void setGrammarRules(String text) { grammarRulesArea.setText(text); }
    public void setFormalDefinition(String text) { formalDefinitionArea.setText(text); }
    public void setCurrentChain(String text) { currentChainArea.setText(text); }
    public void setSteps(String text) { stepsArea.setText(text); }
    public void appendStep(String text) { stepsArea.append(text + "\n"); }
    public void clearSteps() { stepsArea.setText(""); }

    // Методы для работы с таблицей цепочек
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

    // Методы для управления состоянием UI
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

    // Методы для установки слушателей
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

    // Валидация ввода
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
