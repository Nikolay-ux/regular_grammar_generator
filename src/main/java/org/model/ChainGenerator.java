package org.model;

import java.util.*;

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

        for (int length = minLength; length <= maxLength; length++) {
            if (length % grammar.getLengthMultiplicity() != 0) {
                continue;
            }

            generateChainsOfLength("S", length, result, new ArrayList<>(), 0);
        }

        return new ArrayList<>(result);
    }

    private void generateChainsOfLength(String current, int targetLength,
                                        Set<String> result, List<String> path, int depth) {
        if (depth > targetLength * 2 || current.length() > targetLength * 2) {
            return;
        }

        String memoKey = current + "|" + targetLength;
        if (memoization.containsKey(memoKey)) {
            return;
        }

        if (current.length() == targetLength && isTerminalString(current)) {
            if (isValidChain(current)) {
                result.add(current);
                List<String> fullPath = new ArrayList<>(path);
                fullPath.add(current);
                recordGenerationSteps(fullPath);
            }
            memoization.put(memoKey, new HashSet<>());
            return;
        }

        if (current.length() > targetLength) {
            memoization.put(memoKey, new HashSet<>());
            return;
        }

        List<String> nextChains = new ArrayList<>();

        for (int i = 0; i < current.length(); i++) {
            if (Character.isUpperCase(current.charAt(i))) {
                char nonTerminal = current.charAt(i);
                String prefix = current.substring(0, i);
                String suffix = current.substring(i + 1);

                for (GrammarRule rule : grammar.getRules()) {
                    if (rule.getLeftPart().charAt(0) == nonTerminal) {
                        String newChain = prefix + rule.getRightPart() + suffix;
                        nextChains.add(newChain);
                    }
                }

                if (grammar.getType() == GrammarType.LEFT_LINEAR) {
                    break;
                }
            }
        }

        if (nextChains.isEmpty() && !isTerminalString(current)) {
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

        Set<String> generated = new HashSet<>();
        for (String nextChain : nextChains) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(nextChain);
            generateChainsOfLength(nextChain, targetLength, result, newPath, depth + 1);

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
        if (!chain.startsWith(grammar.getInitialChain())) {
            return false;
        }
        if (!chain.endsWith(grammar.getFinalChain())) {
            return false;
        }
        if (chain.length() % grammar.getLengthMultiplicity() != 0) {
            return false;
        }

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
        for (GrammarRule rule : grammar.getRules()) {
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

    public List<ChainGenerationStep> getStepsForChain(String chain) {
        List<ChainGenerationStep> steps = new ArrayList<>();
        int stepNum = 1;

        steps.add(new ChainGenerationStep(stepNum++, "S", "Начальный символ"));

        List<List<String>> allPaths = findAllGenerationPaths("S", chain, new ArrayList<>());

        if (!allPaths.isEmpty()) {
            List<String> path = allPaths.get(0);
            for (int i = 1; i < path.size(); i++) {
                String from = path.get(i-1);
                String to = path.get(i);
                String rule = findRuleBetween(from, to);
                steps.add(new ChainGenerationStep(stepNum++, to,
                        rule != null ? rule : "Шаг генерации"));
            }
        }

        return steps;
    }

    private List<List<String>> findAllGenerationPaths(String current, String target, List<String> path) {
        List<List<String>> result = new ArrayList<>();
        List<String> newPath = new ArrayList<>(path);
        newPath.add(current);

        if (current.equals(target)) {
            result.add(newPath);
            return result;
        }

        if (current.length() > target.length() + 5) {
            return result;
        }

        for (int i = 0; i < current.length(); i++) {
            if (Character.isUpperCase(current.charAt(i))) {
                char nonTerminal = current.charAt(i);

                for (GrammarRule rule : grammar.getRules()) {
                    if (rule.getLeftPart().charAt(0) == nonTerminal) {
                        String newChain = current.substring(0, i) +
                                rule.getRightPart() +
                                current.substring(i + 1);

                        result.addAll(findAllGenerationPaths(newChain, target, newPath));
                    }
                }
                break;
            }
        }

        return result;
    }

    private String findRuleBetween(String from, String to) {
        for (GrammarRule rule : grammar.getRules()) {
            for (int i = 0; i < from.length(); i++) {
                if (from.charAt(i) == rule.getLeftPart().charAt(0)) {
                    String expected = from.substring(0, i) +
                            rule.getRightPart() +
                            from.substring(i + 1);
                    if (expected.equals(to)) {
                        return rule.toString();
                    }
                }
            }
        }
        return null;
    }
}