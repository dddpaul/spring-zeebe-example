package com.github.dddpaul.zeebeexample.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dddpaul.zeebeexample.RiskError;
import com.github.dddpaul.zeebeexample.RiskLevel;
import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.github.dddpaul.zeebeexample.RiskError.RISK_LEVEL_ERROR;

@Component
@ConditionalOnProperty(value = "app.worker.enabled", havingValue = "true")
public class JobWorkers {

    private static final Logger log = LoggerFactory.getLogger(JobWorkers.class);
    private HttpClient client = HttpClient.newHttpClient();

    @Autowired
    private ApplicationStats stats;

    @Data
    static class LoopSettings {
        private int retries;
        private int timeout;
    }

    @JobWorker(type = "loop-settings")
    public Map<String, Object> loopSettings(final ActivatedJob job) {
        int retries = 1;
        int timeout = 20;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://192.168.0.100:10000/params.json"))
                    .timeout(Duration.of(10, ChronoUnit.SECONDS))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LoopSettings loopSettings = new ObjectMapper().readValue(response.body(), LoopSettings.class);
            retries = loopSettings.retries;
            timeout = loopSettings.timeout;
        } catch (Exception e) {
            log.warn("Application {} warning: {}", job.getProcessInstanceKey(), e.getMessage());
        }
        log.info("Application {} loop is configured with retries = {} and timeout = {} minutes", job.getProcessInstanceKey(), retries, timeout);
        return Map.of("retries", retries, "timeout", Duration.ofMinutes(timeout).toString());
    }

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
        stats.incrementApproved();
        log.info("Application {} approved", job.getProcessInstanceKey());
    }

    @JobWorker(type = "reject-app")
    public void reject(final ActivatedJob job) {
        stats.incrementRejected();
        log.info("Application {} rejected", job.getProcessInstanceKey());
    }
}
