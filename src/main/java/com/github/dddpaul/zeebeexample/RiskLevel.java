package com.github.dddpaul.zeebeexample;

import lombok.Getter;

@Getter
public enum RiskLevel {
    GREEN(0), YELLOW(1), RED(2);

    private final int level;

    RiskLevel(int level) {
        this.level = level;
    }
}
