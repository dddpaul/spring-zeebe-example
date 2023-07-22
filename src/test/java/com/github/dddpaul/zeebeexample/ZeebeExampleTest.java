package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import io.camunda.zeebe.process.test.filters.RecordStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@ZeebeProcessTest
public class ZeebeExampleTest {

    private ZeebeTestEngine engine;
    private ZeebeClient client;
    private RecordStream recordStream;

    @Test
    void shouldConnectToZeebe() {
        // given
        final BpmnModelInstance process =
                Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

        // when
        // do something (e.g. deploy a process)
        final DeploymentEvent deploymentEvent =
                client.newDeployCommand().addProcessModel(process, "process.bpmn").send().join();

        // then
        // verify (e.g. we can create an instance of the deployed process)
        final ProcessInstanceResult processInstanceResult =
                client
                        .newCreateInstanceCommand()
                        .bpmnProcessId("process")
                        .latestVersion()
                        .withResult()
                        .send()
                        .join();
        Assertions.assertThat(processInstanceResult.getProcessDefinitionKey())
                .isEqualTo(deploymentEvent.getProcesses().get(0).getProcessDefinitionKey());
    }
}
