package org.model;

import java.util.*;
import java.util.stream.Collectors;

public class ChainGenerator {
    private final RegularGrammar grammar;
    private final List<ChainGenerationStep> generationSteps;
    private final Map<String, Set<String>> memoization;

    public ChainGenerator(RegularGrammar grammar) {
        this.grammar = grammar;
        this.generationSteps = new ArrayList<>();
        this.memoization = new HashMap<>();
    }

    public List<String> generateAllChains(int minLength, int maxLength) {
        generationSteps.clear();
        memoization.clear();
        Set<String> result = new TreeSet<>(Comparator
                .comparingInt(String::length)
                .thenComparing(s -> s));

        // Генерируем цепочки для каждой допустимой длины
        for (int length = minLength; length <= maxLength; length++) {
            if (length % grammar.getLengthMultiplicity() != 0) {
                continue; // Пропускаем длины, не кратные заданной кратности
            }

            generateChainsOfLength("S", length, result, new ArrayList<>(), 0);
        }

        return new ArrayList<>(result);
    }

    private void generateChainsOfLength(String current, int targetLength,
                                        Set<String> result, List<String> path, int depth) {
        // Защита от бесконечной рекурсии
        if (depth > targetLength * 2 || current.length() > targetLength * 2) {
            return;
        }

        String memoKey = current + "|" + targetLength;
        if (memoization.containsKey(memoKey)) {
            // Уже генерировали для этой комбинации
            return;
        }

        // Если текущая цепочка достигла нужной длины и содержит только терминалы
        if (current.length() == targetLength && isTerminalString(current)) {
            if (isValidChain(current)) {
                result.add(current);
                // Сохраняем путь генерации
                List<String> fullPath = new ArrayList<>(path);
                fullPath.add(current);
                recordGenerationSteps(fullPath);
            }
            memoization.put(memoKey, new HashSet<>());
            return;
        }

        // Если превысили длину, останавливаемся
        if (current.length() > targetLength) {
            memoization.put(memoKey, new HashSet<>());
            return;
        }

        // Находим все возможные применения правил
        List<String> nextChains = new ArrayList<>();

        for (int i = 0; i < current.length(); i++) {
            if (Character.isUpperCase(current.charAt(i))) {
                // Нашли нетерминал
                char nonTerminal = current.charAt(i);
                String prefix = current.substring(0, i);
                String suffix = current.substring(i + 1);

                // Применяем все подходящие правила
                for (GrammarRule rule : grammar.getRules()) {
                    if (rule.getLeftPart().charAt(0) == nonTerminal) {
                        String newChain = prefix + rule.getRightPart() + suffix;
                        nextChains.add(newChain);
                    }
                }

                // Для левосторонней грамматики важно обрабатывать самый левый нетерминал
                if (grammar.getType() == GrammarType.LEFT_LINEAR) {
                    break;
                }
            }
        }

        // Если нет нетерминалов, но длина не достигнута
        if (nextChains.isEmpty() && !isTerminalString(current)) {
            // Попытка завершить нетерминалы пустыми правилами
            for (GrammarRule rule : grammar.getRules()) {
                if (rule.getRightPart().isEmpty()) {
                    for (int i = 0; i < current.length(); i++) {
                        if (current.charAt(i) == rule.getLeftPart().charAt(0)) {
                            String newChain = current.substring(0, i) + current.substring(i + 1);
                            nextChains.add(newChain);
                        }
                    }
                }
            }
        }

        // Рекурсивно обрабатываем следующие цепочки
        Set<String> generated = new HashSet<>();
        for (String nextChain : nextChains) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(nextChain);
            generateChainsOfLength(nextChain, targetLength, result, newPath, depth + 1);

            // Запоминаем, что генерировали
            generated.add(nextChain);
        }

        memoization.put(memoKey, generated);
    }

    private boolean isTerminalString(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidChain(String chain) {
        // Проверяем все условия для цепочки
        if (!chain.startsWith(grammar.getInitialChain())) {
            return false;
        }
        if (!chain.endsWith(grammar.getFinalChain())) {
            return false;
        }
        if (chain.length() % grammar.getLengthMultiplicity() != 0) {
            return false;
        }

        // Проверяем, что все символы из алфавита
        for (char c : chain.toCharArray()) {
            if (!grammar.getAlphabet().contains(c)) {
                return false;
            }
        }

        return true;
    }

    private void recordGenerationSteps(List<String> path) {
        for (int i = 0; i < path.size(); i++) {
            String chain = path.get(i);
            String appliedRule = (i == 0) ? "Начало" : findAppliedRule(path.get(i-1), chain);
            generationSteps.add(new ChainGenerationStep(i + 1, chain, appliedRule));
        }
    }

    private String findAppliedRule(String from, String to) {
        // Находим правило, которое преобразовало from в to
        for (GrammarRule rule : grammar.getRules()) {
            // Пытаемся найти, где было применено правило
            for (int i = 0; i < from.length(); i++) {
                if (from.charAt(i) == rule.getLeftPart().charAt(0)) {
                    String expected = from.substring(0, i) + rule.getRightPart() + from.substring(i + 1);
                    if (expected.equals(to)) {
                        return rule.toString();
                    }
                }
            }
        }
        return "Неизвестное правило";
    }

    public List<ChainGenerationStep> getGenerationSteps() {
        return Collections.unmodifiableList(generationSteps);
    }

    public List<ChainGenerationStep> getStepsForChain(String chain) {
        return generationSteps.stream()
                .filter(step -> step.getChain().equals(chain))
                .collect(Collectors.toList());
    }
}