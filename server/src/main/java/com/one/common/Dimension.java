package com.one.common;

public enum Dimension {
    MEAL,
    MILK_TEA,
    COFFEE,
    PRIVATE_HABIT;

    public boolean isShareable() { return this != PRIVATE_HABIT; }
    public boolean isRecommendable() { return this != PRIVATE_HABIT; }
}
