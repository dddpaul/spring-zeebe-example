package com.github.dddpaul.zeebeexample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1;
import io.camunda.zeebe.client.api.command.DeployResourceCommandStep1.DeployResourceCommandStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

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
    void shouldStartDeployedProcess() throws JsonProcessingException {
        // given
        Map<String, String> variables = Map.of("chance", "0");

        // when
        final ProcessInstanceResult result = client
                .newCreateInstanceCommand()
                .bpmnProcessId(PROCESS_ID)
                .latestVersion()
                .variables(objectMapper.writeValueAsString(variables))
                .withResult()
                .send()
                .join();

        // then
        System.out.println("result.getVariables() = " + result.getVariables());
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
}
