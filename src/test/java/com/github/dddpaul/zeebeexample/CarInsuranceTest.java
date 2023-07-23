package com.github.dddpaul.zeebeexample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@ZeebeProcessTest
public class CarInsuranceTest {

    public static final String PROCESS_ID = "CarInsuranceApplicationProcess";
    public static final String PROCESS_RESOURCE_NAME = "car-insurance-application.bpmn";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ZeebeTestEngine engine;
    private ZeebeClient client;
    private RecordStream recordStream;

    /**
     * Use method from <a href="https://github.com/camunda/zeebe-process-test/blob/main/examples/src/test/java/io/camunda/zeebe/process/test/examples/PullRequestProcessTest.java">PullRequestProcessTest.java</a>
     */
    @BeforeEach
    void deployProcesses() {
        DeploymentEvent event = deployResources(PROCESS_RESOURCE_NAME);
        BpmnAssert.assertThat(event).containsProcessesByResourceName(PROCESS_RESOURCE_NAME);
    }

    @Test
    void shouldStartDeployedProcess() throws JsonProcessingException, InterruptedException, TimeoutException {
        // given
        Map<String, String> variables = Map.of("chance", "0");

        // when
        ProcessInstanceEvent event = client
                .newCreateInstanceCommand()
                .bpmnProcessId(PROCESS_ID)
                .latestVersion()
                .variables(objectMapper.writeValueAsString(variables))
                .send()
                .join();
        completeServiceTask("risk-level", 1, Map.of("riskLevel", "green"));
        completeServiceTask("approve-app", 1, Map.of());

        // then
        BpmnAssert.assertThat(event)
                .hasPassedElement("Task_ApproveApplication")
                .isCompleted();
    }

    private DeploymentEvent deployResources(final String... resources) {
        final DeployResourceCommandStep1 step1 = client.newDeployResourceCommand();
        DeployResourceCommandStep2 step2 = null;
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

    /**
     * These two methods deal with the asynchronous nature of the engine. It is recommended
     * to wait for an idle state before you assert on the state of the engine. Otherwise, you
     * may run into race conditions and flaky tests, depending on whether the engine
     * is still busy processing your last commands.
     * <p>
     * Also note that many of the helper functions used in this test (e.g. {@code sendMessage(..)}
     * have a call to this method at the end. This is to ensure that each command sent to the engine
     * is fully processed before moving on. Without that you can run into issues, where e.g. you want
     * to complete a task, but the task has not been activated yet.
     * <p>
     * Note that the duration is not like a {@code Thread.sleep()}. The tests will continue as soon as
     * an idle state is reached. Only if no idle state is reached during the {@code duration}
     * passed in as argument, then a timeout exception will be thrown.
     */
    private void waitForIdleState(final Duration duration)
            throws InterruptedException, TimeoutException {
        engine.waitForIdleState(duration);
    }

    private void waitForBusyState(final Duration duration)
            throws InterruptedException, TimeoutException {
        engine.waitForBusyState(duration);
    }

    private void completeServiceTask(String jobType, int count, Map<String, String> variables) throws InterruptedException, TimeoutException {
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

}
