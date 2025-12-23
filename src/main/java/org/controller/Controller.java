package org.controller;

import org.model.*;
import org.view.MainFrame;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Controller {
    private final MainFrame view;
    private RegularGrammar grammar;
    private ChainGenerator chainGenerator;
    private List<String> generatedChains;
    private Timer stepTimer;
    private int currentChainIndex;
    private int currentStepIndex;
    private List<ChainGenerationStep> currentChainSteps;

    public Controller(MainFrame view) {
        this.view = view;
        this.generatedChains = new ArrayList<>();

        setupListeners();
        initializeUI();
    }

    private void setupListeners() {
        view.setGenerateGrammarListener(e -> generateGrammar());
        view.setGenerateChainsListener(e -> generateChains());
        view.setStepByStepListener(e -> startStepByStepGeneration());
        view.setExportListener(e -> exportResults());
        view.setClearListener(e -> clearAll());
    }

    private void initializeUI() {
        view.setGrammarRules("Грамматика еще не построена");
        view.setFormalDefinition("Формальное определение будет отображено после построения грамматики");
        view.setCurrentChain("Текущая цепочка будет отображена здесь");
        view.setSteps("Шаги генерации будут отображены здесь");
    }

    private void generateGrammar() {
        if (!view.validateInput()) {
            return;
        }

        try {
            String alphabetText = view.getAlphabetText();
            String initialChain = view.getInitialChain();
            String finalChain = view.getFinalChain();
            int multiplicity = view.getMultiplicity();
            GrammarType type = view.getGrammarType();

            Set<Character> alphabet = parseAlphabet(alphabetText);

            if (alphabet.isEmpty()) {
                view.setErrorStatus("Алфавит должен содержать хотя бы один символ");
                return;
            }

            if (!validateChain(initialChain, alphabet)) {
                view.setErrorStatus("Начальная цепочка содержит символы не из алфавита");
                return;
            }

            if (!validateChain(finalChain, alphabet)) {
                view.setErrorStatus("Конечная цепочка содержит символы не из алфавита");
                return;
            }

            grammar = new RegularGrammar(type, alphabet, initialChain, finalChain, multiplicity);

            displayGrammar();

            view.setSuccessStatus("Грамматика успешно построена");

        } catch (Exception ex) {
            view.setErrorStatus("Ошибка при построении грамматики: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Set<Character> parseAlphabet(String alphabetText) {
        Set<Character> alphabet = new HashSet<>();
        for (char c : alphabetText.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                alphabet.add(c);
            }
        }
        return alphabet;
    }

    private boolean validateChain(String chain, Set<Character> alphabet) {
        if (chain == null || chain.isEmpty()) {
            return true;
        }

        for (char c : chain.toCharArray()) {
            if (!alphabet.contains(c)) {
                return false;
            }
        }
        return true;
    }

    private void displayGrammar() {
        if (grammar == null) return;

        StringBuilder rulesText = new StringBuilder();
        rulesText.append("Тип грамматики: ").append(grammar.getType()).append("\n\n");
        rulesText.append("Правила грамматики:\n");

        int ruleNumber = 1;
        for (GrammarRule rule : grammar.getRules()) {
            rulesText.append(String.format("%3d. %s\n", ruleNumber++, rule));
        }

        view.setGrammarRules(rulesText.toString());

        view.setFormalDefinition(grammar.getFormalDefinition());
    }

    private void generateChains() {
        if (grammar == null) {
            JOptionPane.showMessageDialog(view,
                    "Сначала постройте грамматику",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int minLength = view.getMinLength();
            int maxLength = view.getMaxLength();

            if (minLength > maxLength) {
                view.setErrorStatus("Минимальная длина больше максимальной");
                return;
            }

            view.showProgress(true);
            view.setStatus("Генерация цепочек...");

            SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<String> doInBackground() throws Exception {
                    chainGenerator = new ChainGenerator(grammar);
                    return chainGenerator.generateAllChains(minLength, maxLength);
                }

                @Override
                protected void done() {
                    try {
                        generatedChains = get();
                        displayGeneratedChains();
                        view.setSuccessStatus(
                                String.format("Сгенерировано %d цепочек", generatedChains.size()));
                    } catch (InterruptedException | ExecutionException ex) {
                        view.setErrorStatus("Ошибка при генерации: " + ex.getMessage());
                        ex.printStackTrace();
                    } finally {
                        view.showProgress(false);
                    }
                }
            };

            worker.execute();

        } catch (Exception ex) {
            view.setErrorStatus("Ошибка: " + ex.getMessage());
            view.showProgress(false);
        }
    }

    private void displayGeneratedChains() {
        view.clearChainsTable();

        for (int i = 0; i < generatedChains.size(); i++) {
            String chain = generatedChains.get(i);

            boolean valid = true;
            if (!chain.startsWith(grammar.getInitialChain())) {
                valid = false;
            }
            if (!chain.endsWith(grammar.getFinalChain())) {
                valid = false;
            }
            if (chain.length() % grammar.getLengthMultiplicity() != 0) {
                valid = false;
            }

            view.addChainToTable(i + 1, chain, valid);
        }

        if (generatedChains.isEmpty()) {
            view.addChainToTable(1, "Цепочки не найдены", false);
        }
    }

    private void startStepByStepGeneration() {
        if (generatedChains.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Сначала сгенерируйте цепочки",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (stepTimer != null && stepTimer.isRunning()) {
            stepTimer.stop();
        }

        currentChainIndex = 0;
        currentStepIndex = 0;

        if (chainGenerator != null) {
            currentChainSteps = chainGenerator.getStepsForChain(
                    generatedChains.get(currentChainIndex));
        } else {
            currentChainSteps = new ArrayList<>();
        }

        view.clearSteps();
        view.setCurrentChain("Выберите цепочку для пошагового просмотра");

        String[] chainArray = generatedChains.toArray(new String[0]);
        String selectedChain = (String) JOptionPane.showInputDialog(view,
                "Выберите цепочку для пошаговой генерации:",
                "Выбор цепочки",
                JOptionPane.PLAIN_MESSAGE,
                null,
                chainArray,
                chainArray[0]);

        if (selectedChain != null) {
            currentChainIndex = generatedChains.indexOf(selectedChain);
            if (currentChainIndex >= 0) {
                startStepByStepForChain(selectedChain);
            }
        }
    }

    private void startStepByStepForChain(String chain) {
        view.setCurrentChain("Цепочка: " + chain + " (длина: " + chain.length() + ")");
        view.clearSteps();

        if (chainGenerator != null) {
            currentChainSteps = chainGenerator.getStepsForChain(chain);
        }

        currentStepIndex = 0;

        stepTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStepIndex < currentChainSteps.size()) {
                    ChainGenerationStep step = currentChainSteps.get(currentStepIndex);
                    view.appendStep(step.toString());
                    currentStepIndex++;
                } else {
                    ((Timer) e.getSource()).stop();
                    view.appendStep("\nГенерация завершена!");
                }
            }
        });

        stepTimer.start();
    }

    private void exportResults() {
        if (grammar == null && generatedChains.isEmpty()) {
            JOptionPane.showMessageDialog(view,
                    "Нет данных для экспорта",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Экспорт результатов");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Текстовые файлы (*.txt)", "txt"));

        if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Результаты работы генератора регулярных грамматик");
                writer.println("=".repeat(50));
                writer.println();

                if (grammar != null) {
                    writer.println("ПАРАМЕТРЫ ГРАММАТИКИ:");
                    writer.println("-".repeat(30));
                    writer.println("Тип грамматики: " + grammar.getType());
                    writer.println("Алфавит: " + grammar.getAlphabet());
                    writer.println("Начальная подцепочка: " +
                            (grammar.getInitialChain().isEmpty() ? "(пусто)" : grammar.getInitialChain()));
                    writer.println("Конечная подцепочка: " +
                            (grammar.getFinalChain().isEmpty() ? "(пусто)" : grammar.getFinalChain()));
                    writer.println("Кратность длины: " + grammar.getLengthMultiplicity());
                    writer.println();

                    writer.println("ПРАВИЛА ГРАММАТИКИ:");
                    writer.println("-".repeat(30));
                    int ruleNum = 1;
                    for (GrammarRule rule : grammar.getRules()) {
                        writer.printf("%3d. %s%n", ruleNum++, rule);
                    }
                    writer.println();
                }

                if (!generatedChains.isEmpty()) {
                    writer.println("СГЕНЕРИРОВАННЫЕ ЦЕПОЧКИ:");
                    writer.println("-".repeat(30));
                    writer.printf("Всего цепочек: %d%n", generatedChains.size());
                    writer.printf("Диапазон длин: %d - %d%n",
                            view.getMinLength(), view.getMaxLength());
                    writer.println();

                    for (int i = 0; i < generatedChains.size(); i++) {
                        String chain = generatedChains.get(i);
                        writer.printf("%4d. %s (длина: %d)%n",
                                i + 1, chain, chain.length());
                    }
                }

                writer.println();
                writer.println("Экспорт выполнен: " + new Date());

                view.setSuccessStatus("Результаты экспортированы в " + file.getName());

            } catch (IOException ex) {
                view.setErrorStatus("Ошибка при экспорте: " + ex.getMessage());
            }
        }
    }

    private void clearAll() {
        int result = JOptionPane.showConfirmDialog(view,
                "Вы уверены, что хотите очистить все данные?",
                "Подтверждение очистки",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            grammar = null;
            chainGenerator = null;
            generatedChains.clear();

            view.setGrammarRules("");
            view.setFormalDefinition("");
            view.clearChainsTable();
            view.setCurrentChain("");
            view.setSteps("");

            view.setStatus("Все данные очищены");
        }
    }
}