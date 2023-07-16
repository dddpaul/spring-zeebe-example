package com.github.dddpaul.zeebeexample.workers;

import com.github.dddpaul.zeebeexample.RiskLevel;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(value = "app.worker.enabled", havingValue = "true")
public class JobWorkers {

    private static final Logger log = LoggerFactory.getLogger(JobWorkers.class);

    @JobWorker(type = "risk-level")
    public Map<String, Object> riskLevel(final ActivatedJob job, @Variable int chance) {
        try {
            return Map.of("riskLevel", RiskLevel.values()[chance].name().toLowerCase());
        } catch (Exception e) {
            log.error("Error while handle {} application: {}", job.getProcessInstanceKey(), e.getMessage());
            throw new RuntimeException(e);
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
