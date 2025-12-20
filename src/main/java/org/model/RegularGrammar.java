package org.model;

import java.util.*;
import java.util.stream.Collectors;

public class RegularGrammar {
    private final GrammarType type;
    private final Set<Character> alphabet;
    private final String initialChain;
    private final String finalChain;
    private final int lengthMultiplicity;
    private final List<GrammarRule> rules;
    private final Map<Integer, Character> stateToNonTerminal = new HashMap<>();
    private final Map<Character, Integer> nonTerminalToState = new HashMap<>();
    private char currentNonTerminal = 'A';

    public RegularGrammar(GrammarType type, Set<Character> alphabet,
                          String initialChain, String finalChain, int lengthMultiplicity) {
        this.type = type;
        this.alphabet = new HashSet<>(alphabet);
        this.initialChain = initialChain != null ? initialChain : "";
        this.finalChain = finalChain != null ? finalChain : "";
        this.lengthMultiplicity = Math.max(1, lengthMultiplicity);
        this.rules = new ArrayList<>();

        generateGrammar();
    }

    private void generateGrammar() {
        rules.clear();
        currentNonTerminal = 'A';
        stateToNonTerminal.clear();
        nonTerminalToState.clear();

        for (int i = 0; i < lengthMultiplicity; i++) {
            char nonTerm = nextNonTerminal();
            stateToNonTerminal.put(i, nonTerm);
            nonTerminalToState.put(nonTerm, i);
        }

        if (type == GrammarType.RIGHT_LINEAR) {
            generateRightLinearGrammar();
        } else {
            generateLeftLinearGrammar();
        }
    }

    private void generateRightLinearGrammar() {
        if (!initialChain.isEmpty()) {
            int stateAfterInitial = (initialChain.length() % lengthMultiplicity);
            char targetState = stateToNonTerminal.get(stateAfterInitial);
            rules.add(new GrammarRule("S", initialChain + targetState,
                    "Начальная подцепочка и переход в состояние " + targetState));
        } else {
            rules.add(new GrammarRule("S", String.valueOf(stateToNonTerminal.get(0)),
                    "Начало с состояния A"));
        }

        for (int currentState = 0; currentState < lengthMultiplicity; currentState++) {
            char currentNonTerm = stateToNonTerminal.get(currentState);
            int nextState = (currentState + 1) % lengthMultiplicity;
            char nextNonTerm = stateToNonTerminal.get(nextState);

            for (char symbol : alphabet) {
                rules.add(new GrammarRule(String.valueOf(currentNonTerm),
                        String.valueOf(symbol) + String.valueOf(nextNonTerm),
                        String.format("Генерация %s: состояние %d→%d (остаток %d→%d)",
                                symbol, currentState, nextState, currentState, nextState)));
            }
        }

        if (!finalChain.isEmpty()) {
            addFinalChainRulesRightLinear();
        } else {
            char zeroState = stateToNonTerminal.get(0);
            rules.add(new GrammarRule(String.valueOf(zeroState), "",
                    "Пустое завершение из состояния 0"));
        }
    }

    private void addFinalChainRulesRightLinear() {
        int finalLength = finalChain.length();

        for (int state = 0; state < lengthMultiplicity; state++) {
            if ((state + finalLength) % lengthMultiplicity == 0) {
                char startState = stateToNonTerminal.get(state);

                if (finalChain.length() == 1) {
                    rules.add(new GrammarRule(String.valueOf(startState), finalChain,
                            "Завершение конечной подцепочкой из состояния " + state));
                } else {
                    String currentLeft = String.valueOf(startState);

                    for (int i = 0; i < finalChain.length(); i++) {
                        char symbol = finalChain.charAt(i);

                        if (i == finalChain.length() - 1) {
                            rules.add(new GrammarRule(currentLeft,
                                    String.valueOf(symbol),
                                    "Завершение конечной подцепочки из состояния " + state));
                        } else {
                            int nextState = (state + i + 1) % lengthMultiplicity;
                            char nextNonTerm;

                            if (stateToNonTerminal.containsKey(nextState)) {
                                nextNonTerm = stateToNonTerminal.get(nextState);
                            } else {
                                nextNonTerm = nextNonTerminal();
                                stateToNonTerminal.put(nextState, nextNonTerm);
                                nonTerminalToState.put(nextNonTerm, nextState);
                            }

                            rules.add(new GrammarRule(currentLeft,
                                    String.valueOf(symbol) + String.valueOf(nextNonTerm),
                                    String.format("Генерация символа %s конечной подцепочки (шаг %d)", symbol, i)));
                            currentLeft = String.valueOf(nextNonTerm);
                        }
                    }
                }
            }
        }
    }

    private void generateLeftLinearGrammar() {
        if (!finalChain.isEmpty()) {
            int stateBeforeFinal = (lengthMultiplicity - (finalChain.length() % lengthMultiplicity)) % lengthMultiplicity;
            char startState = stateToNonTerminal.get(stateBeforeFinal);
            rules.add(new GrammarRule("S", startState + finalChain,
                    String.format("Переход в состояние %d и добавление конечной подцепочки", stateBeforeFinal)));
        } else {
            rules.add(new GrammarRule("S", String.valueOf(stateToNonTerminal.get(0)),
                    "Начало с состояния A"));
        }

        for (int currentState = 0; currentState < lengthMultiplicity; currentState++) {
            char currentNonTerm = stateToNonTerminal.get(currentState);
            int prevState = (currentState - 1 + lengthMultiplicity) % lengthMultiplicity;
            char prevNonTerm = stateToNonTerminal.get(prevState);

            for (char symbol : alphabet) {
                rules.add(new GrammarRule(String.valueOf(currentNonTerm),
                        String.valueOf(prevNonTerm) + String.valueOf(symbol),
                        String.format("Генерация %s: состояние %d→%d (остаток %d→%d)",
                                symbol, currentState, prevState, currentState, prevState)));
            }
        }

        if (!initialChain.isEmpty()) {
            addInitialChainRulesLeftLinear();
        } else {
            char zeroState = stateToNonTerminal.get(0);
            rules.add(new GrammarRule(String.valueOf(zeroState), "",
                    "Пустое завершение из состояния 0"));
        }
    }

    private void addInitialChainRulesLeftLinear() {
        int stateBeforeInitial = (initialChain.length() % lengthMultiplicity);
        char startStateForInitial = stateToNonTerminal.get(stateBeforeInitial);

        List<Character> reversedInitial = new ArrayList<>();
        for (int i = initialChain.length() - 1; i >= 0; i--) {
            reversedInitial.add(initialChain.charAt(i));
        }

        String currentLeft = String.valueOf(startStateForInitial);

        for (int i = 0; i < reversedInitial.size(); i++) {
            char symbol = reversedInitial.get(i);

            if (i == reversedInitial.size() - 1) {
                rules.add(new GrammarRule(currentLeft,
                        String.valueOf(symbol),
                        "Завершение начальной подцепочки"));
            } else {
                char nextNonTerm = nextNonTerminal();
                int nextState = (stateBeforeInitial - (i + 1) + lengthMultiplicity) % lengthMultiplicity;
                stateToNonTerminal.put(nextState, nextNonTerm);
                nonTerminalToState.put(nextNonTerm, nextState);

                rules.add(new GrammarRule(currentLeft,
                        nextNonTerm + String.valueOf(symbol),
                        String.format("Генерация символа %s начальной подцепочки", symbol)));
                currentLeft = String.valueOf(nextNonTerm);
            }
        }

        if (!finalChain.isEmpty()) {
            int stateAfterFinal = (nonTerminalToState.get(stateToNonTerminal.get(
                    (lengthMultiplicity - (finalChain.length() % lengthMultiplicity)) % lengthMultiplicity))
                    + finalChain.length()) % lengthMultiplicity;

            if (stateAfterFinal == stateBeforeInitial) {
            } else {
                addStateTransitionRules(stateAfterFinal, stateBeforeInitial);
            }
        }
    }

    private void addStateTransitionRules(int fromState, int toState) {
        char fromNonTerm = stateToNonTerminal.get(fromState);
        char toNonTerm = stateToNonTerminal.get(toState);

        if (type == GrammarType.RIGHT_LINEAR) {
            int steps = (toState - fromState + lengthMultiplicity) % lengthMultiplicity;
            if (steps > 0) {
                String currentLeft = String.valueOf(fromNonTerm);
                for (int i = 0; i < steps; i++) {
                    char intermediateNonTerm = (i == steps - 1) ? toNonTerm : nextNonTerminal();
                    int intermediateState = (fromState + i + 1) % lengthMultiplicity;

                    if (i != steps - 1) {
                        stateToNonTerminal.put(intermediateState, intermediateNonTerm);
                        nonTerminalToState.put(intermediateNonTerm, intermediateState);
                    }

                    for (char symbol : alphabet) {
                        rules.add(new GrammarRule(currentLeft,
                                String.valueOf(symbol) + String.valueOf(intermediateNonTerm),
                                String.format("Переход к состоянию %d", intermediateState)));
                    }

                    currentLeft = String.valueOf(intermediateNonTerm);
                }
            }
        } else {
            int steps = (fromState - toState + lengthMultiplicity) % lengthMultiplicity;
            if (steps > 0) {
                String currentLeft = String.valueOf(fromNonTerm);
                for (int i = 0; i < steps; i++) {
                    char intermediateNonTerm = (i == steps - 1) ? toNonTerm : nextNonTerminal();
                    int intermediateState = (fromState - i - 1 + lengthMultiplicity) % lengthMultiplicity;

                    if (i != steps - 1) {
                        stateToNonTerminal.put(intermediateState, intermediateNonTerm);
                        nonTerminalToState.put(intermediateNonTerm, intermediateState);
                    }

                    for (char symbol : alphabet) {
                        rules.add(new GrammarRule(currentLeft,
                                String.valueOf(intermediateNonTerm) + String.valueOf(symbol),
                                String.format("Переход к состоянию %d", intermediateState)));
                    }

                    currentLeft = String.valueOf(intermediateNonTerm);
                }
            }
        }
    }

    private char nextNonTerminal() {
        char result = currentNonTerminal;
        currentNonTerminal++;
        if (currentNonTerminal > 'Z') {
            currentNonTerminal = 'A';
        }
        return result;
    }

    public GrammarType getType() { return type; }
    public Set<Character> getAlphabet() { return Collections.unmodifiableSet(alphabet); }
    public String getInitialChain() { return initialChain; }
    public String getFinalChain() { return finalChain; }
    public int getLengthMultiplicity() { return lengthMultiplicity; }
    public List<GrammarRule> getRules() { return Collections.unmodifiableList(rules); }

    public String getFormalDefinition() {
        StringBuilder sb = new StringBuilder();
        sb.append("G = (V, Σ, P, S)\n");
        sb.append("где:\n");

        sb.append("V = {");
        Set<String> nonTerminals = new TreeSet<>();
        nonTerminals.add("S");
        for (GrammarRule rule : rules) {
            String left = rule.getLeftPart();
            if (left.length() == 1 && Character.isUpperCase(left.charAt(0))) {
                nonTerminals.add(left);
            }
            for (char c : rule.getRightPart().toCharArray()) {
                if (Character.isUpperCase(c)) {
                    nonTerminals.add(String.valueOf(c));
                }
            }
        }
        sb.append(String.join(", ", nonTerminals)).append("}\n");

        sb.append("Σ = {");
        sb.append(alphabet.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
        sb.append("}\n");

        sb.append("S - начальный символ\n");

        sb.append("P = {\n");
        for (GrammarRule rule : rules) {
            sb.append("    ").append(rule).append("\n");
        }
        sb.append("}\n");

        return sb.toString();
    }
}