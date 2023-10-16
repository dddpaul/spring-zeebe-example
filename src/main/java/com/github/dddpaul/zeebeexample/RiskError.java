package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import lombok.Getter;

import java.util.Map;

@Getter
public enum RiskError {

    RISK_LEVEL_ERROR(
            "RISK_LEVEL_ERROR",
            "Some error just happened: %s"
    );

    private final String code;

    private final String message;

    RiskError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ZeebeBpmnError create(RiskError e) {
        return new ZeebeBpmnError(e.getCode(), e.getMessage());
    }

    public static ZeebeBpmnError create(RiskError e, Object... args) {
        return new ZeebeBpmnError(e.getCode(), e.getMessage().formatted(args), Map.of());
    }
}
