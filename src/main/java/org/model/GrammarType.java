package org.model;

public enum GrammarType {
    LEFT_LINEAR("Левосторонняя (ЛЛ)"),
    RIGHT_LINEAR("Правосторонняя (ПЛ)");

    private final String description;

    GrammarType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
