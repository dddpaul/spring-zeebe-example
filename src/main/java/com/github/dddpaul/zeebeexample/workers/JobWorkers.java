package com.github.dddpaul.zeebeexample.workers;

import com.github.dddpaul.zeebeexample.RiskError;
import com.github.dddpaul.zeebeexample.RiskLevel;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.github.dddpaul.zeebeexample.RiskError.RISK_LEVEL_ERROR;

@Component
@ConditionalOnProperty(value = "app.worker.enabled", havingValue = "true")
public class JobWorkers {

    private static final Logger log = LoggerFactory.getLogger(JobWorkers.class);

    @JobWorker(type = "risk-level")
    public Map<String, Object> riskLevel(final ActivatedJob job, @Variable int chance) {
        try {
            if (chance >= RiskLevel.values().length) {
                throw new RuntimeException("chance = %d is not acceptable".formatted(chance));
            }
            return Map.of("riskLevel", RiskLevel.values()[chance].name().toLowerCase());
        } catch (Exception e) {
            log.error("Application {} error: {}", job.getProcessInstanceKey(), e.getMessage());
            throw RiskError.create(RISK_LEVEL_ERROR, e.getMessage());
        }
    }

    @JobWorker(type = "approve-app")
    public void approve(final ActivatedJob job) {
        log.info("Application {} approved", job.getProcessInstanceKey());
    }

    @JobWorker(type = "reject-app")
    public void reject(final ActivatedJob job) {
        log.info("Application {} rejected", job.getProcessInstanceKey());
    }
}
