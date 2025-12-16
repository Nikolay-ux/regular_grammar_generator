package org.model;

public class GrammarRule {
    private final String leftPart;
    private final String rightPart;
    private final String description;

    public GrammarRule(String leftPart, String rightPart, String description) {
        this.leftPart = leftPart;
        this.rightPart = rightPart;
        this.description = description;
    }

    public GrammarRule(String leftPart, String rightPart) {
        this(leftPart, rightPart, "");
    }

    public String getLeftPart() { return leftPart; }
    public String getRightPart() { return rightPart; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return leftPart + " → " + rightPart +
                (description.isEmpty() ? "" : " (" + description + ")");
    }
}