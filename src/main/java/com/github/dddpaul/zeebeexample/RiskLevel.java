package com.github.dddpaul.zeebeexample;

public enum RiskLevel {
    GREEN(0), YELLOW(1), RED(2);

    private final int level;

    RiskLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
