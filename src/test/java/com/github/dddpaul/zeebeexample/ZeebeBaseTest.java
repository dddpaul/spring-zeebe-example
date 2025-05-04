package com.github.dddpaul.zeebeexample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@ZeebeProcessTest
public class ZeebeBaseTest {

    private ZeebeClient client;
    private ZeebeTestEngine engine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeploymentEvent deployResources(String... resources) {
        final DeployResourceCommandStep1 step1 = client.newDeployResourceCommand();
        DeployResourceCommandStep1.DeployResourceCommandStep2 step2 = null;
        for (String process : resources) {
            if (step2 == null) {
                step2 = step1.addResourceFromClasspath(process);
            } else {
                step2 = step2.addResourceFromClasspath(process);
            }
        }
        return Objects.requireNonNull(step2)
                .send()
                .join();
    }

    public ProcessInstanceEvent createProcessInstance(String processId, Map<String, String> variables) throws JsonProcessingException {
        return client
                .newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(objectMapper.writeValueAsString(variables))
                .send()
                .join();
    }

    public PublishMessageResponse sendMessage(String messageName, String correlationKey, Map<String, String> variables) throws InterruptedException, TimeoutException {
        PublishMessageResponse response = client
                        .newPublishMessageCommand()
                        .messageName(messageName)
                        .correlationKey(correlationKey)
                        .variables(variables)
                        .send()
                        .join();
        waitForIdleState(Duration.ofSeconds(1));
        return response;
    }

    public void completeServiceTask(String jobType, int count) throws InterruptedException, TimeoutException {
        completeServiceTask(jobType, count, Map.of());
    }

    public void completeServiceTask(String jobType, int count, Map<String, String> variables) throws InterruptedException, TimeoutException {
        ActivateJobsResponse response = client.newActivateJobsCommand()
                .jobType(jobType)
                .maxJobsToActivate(count)
                .send()
                .join();

        int activatedJobCount = response.getJobs().size();
        if (activatedJobCount < count) {
            Assertions.fail("Unable to activate %d jobs, because only %d were activated.".formatted(count, activatedJobCount));
        }

        for (int i = 0; i < count; i++) {
            ActivatedJob job = response.getJobs().get(i);
            client.newCompleteCommand(job.getKey())
                    .variables(variables)
                    .send()
                    .join();
        }
        waitForIdleState(Duration.ofSeconds(1));
    }

    public void throwErrorServiceTask(String jobType, int count) throws InterruptedException, TimeoutException {
        throwErrorServiceTask(jobType, count, Map.of());
    }


    public void throwErrorServiceTask(String jobType, int count, Map<String, String> variables) throws InterruptedException, TimeoutException {
        ActivateJobsResponse response = client.newActivateJobsCommand()
                .jobType(jobType)
                .maxJobsToActivate(count)
                .send()
                .join();

        int activatedJobCount = response.getJobs().size();
        if (activatedJobCount < count) {
            Assertions.fail("Unable to activate %d jobs, because only %d were activated.".formatted(count, activatedJobCount));
        }

        for (int i = 0; i < count; i++) {
            ActivatedJob job = response.getJobs().get(i);
            client.newThrowErrorCommand(job.getKey())
                    .errorCode(RiskError.RISK_LEVEL_ERROR.getCode())
                    .variables(variables)
                    .send()
                    .join();
        }
        waitForIdleState(Duration.ofSeconds(1));
    }

    public void evaluateDecision(String decisionId, int count) {
        evaluateDecision(decisionId, count, Map.of());
    }

    public void evaluateDecision(String decisionId, int count, Map<String, String> variables) {
        EvaluateDecisionResponse response = client.newEvaluateDecisionCommand()
                .decisionId(decisionId)
                .variables(variables)
                .send()
                .join();

        int decisions = response.getEvaluatedDecisions().size();
        if (decisions < count) {
            Assertions.fail("Unable to evaluate %d decisions, because only %d were evaluated.".formatted(count, decisions));
        }
    }

    /**
     * These two methods deal with the asynchronous nature of the engine. It is recommended
     * to wait for an idle state before you assert on the state of the engine. Otherwise, you
     * may run into race conditions and flaky tests, depending on whether the engine
     * is still busy processing your last commands.
     * <p>
     * Also note that many of the helper functions used in this test have a call to this method at the end.
     * This is to ensure that each command sent to the engine is fully processed before moving on.
     * Without that you can run into issues, where e.g. you want
     * to complete a task, but the task has not been activated yet.
     * <p>
     * Note that the duration is not like a {@code Thread.sleep()}. The tests will continue as soon as
     * an idle state is reached. Only if no idle state is reached during the {@code duration}
     * passed in as argument, then a timeout exception will be thrown.
     */
    public void waitForIdleState(Duration duration) throws InterruptedException, TimeoutException {
        engine.waitForIdleState(duration);
    }

    public void waitForBusyState(Duration duration) throws InterruptedException, TimeoutException {
        engine.waitForBusyState(duration);
    }
}
