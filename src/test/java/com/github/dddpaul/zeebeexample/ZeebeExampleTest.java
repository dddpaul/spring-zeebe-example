package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@ZeebeProcessTest
public class ZeebeExampleTest {

    private ZeebeTestEngine engine;
    private ZeebeClient client;
    private RecordStream recordStream;

    @Test
    void shouldStartDeployedProcess() {
        // given
        final BpmnModelInstance process = Bpmn.createExecutableProcess("process")
                .startEvent()
                .endEvent()
                .done();

        // when
        final DeploymentEvent deploymentEvent = client.newDeployResourceCommand()
                .addProcessModel(process, "process.bpmn")
                .send()
                .join();

        // then
        final ProcessInstanceResult result = client
                .newCreateInstanceCommand()
                .bpmnProcessId("process")
                .latestVersion()
                .withResult()
                .send()
                .join();

        assertThat(result.getProcessDefinitionKey())
                .isEqualTo(deploymentEvent.getProcesses().get(0).getProcessDefinitionKey());
    }
}
