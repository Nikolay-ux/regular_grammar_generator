package org.model;

public class ChainGenerationStep {
    private final int stepNumber;
    private final String chain;
    private final String appliedRule;
    private final String description;

    public ChainGenerationStep(int stepNumber, String chain,
                               String appliedRule, String description) {
        this.stepNumber = stepNumber;
        this.chain = chain;
        this.appliedRule = appliedRule;
        this.description = description;
    }

    public ChainGenerationStep(int stepNumber, String chain, String appliedRule) {
        this(stepNumber, chain, appliedRule, "");
    }

    public int getStepNumber() { return stepNumber; }
    public String getChain() { return chain; }
    public String getAppliedRule() { return appliedRule; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("Шаг %d: %s → %s%s",
                stepNumber,
                chain,
                appliedRule,
                description.isEmpty() ? "" : " (" + description + ")");
    }
}
