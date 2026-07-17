package com.one.record;

public enum SugarLevel {
    NO_SUGAR, LOW, THREE, FIVE, SEVEN, FULL, NORMAL, UNKNOWN;
    public SugarLevel oneStepLower() {
        return switch (this) {
            case FULL -> SEVEN;
            case SEVEN, NORMAL -> FIVE;
            case FIVE -> THREE;
            case THREE -> LOW;
            case LOW -> NO_SUGAR;
            default -> this;
        };
    }
}
