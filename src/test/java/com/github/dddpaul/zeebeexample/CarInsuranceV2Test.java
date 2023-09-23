package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CarInsuranceV2Test extends ZeebeBaseTest {

    public static final String PROCESS_ID = "CarInsuranceApplicationProcessV2";
    public static final String PROCESS_RESOURCE_NAME = "car-insurance-application-v2.bpmn";
    public static final String DECISION_RESOURCE_NAME = "car-insurance-delay-calculator.dmn";

    @BeforeEach
    void deployProcesses() {
        DeploymentEvent event = deployResources(PROCESS_RESOURCE_NAME, DECISION_RESOURCE_NAME);
        BpmnAssert.assertThat(event).containsProcessesByResourceName(PROCESS_RESOURCE_NAME);
    }

    @Test
    void shouldApproveApplicationWhenRiskLevelIsGreen() {
        // given
        ProcessInstanceEvent flow = createProcessInstance(PROCESS_ID, Map.of());

        // when
        completeServiceTask("risk-level", 1, Map.of("riskLevel", "green"));
        completeServiceTask("approve-app", 1);

        // then
        BpmnAssert.assertThat(flow)
                .hasPassedElement("Task_ApproveApplication")
                .isCompleted();
    }

    @Test
    void shouldRejectApplicationWhenRiskLevelIsRed() {
        // given
        ProcessInstanceEvent flow = createProcessInstance(PROCESS_ID, Map.of());

        // when
        completeServiceTask("risk-level", 1, Map.of("riskLevel", "red"));
        completeServiceTask("reject-app", 1);

        // then
        BpmnAssert.assertThat(flow)
                .hasPassedElement("Task_RejectApplication")
                .isCompleted();
    }

    @Test
    void shouldDelayWhenRiskLevelIsUndefined() {
        // given
        ProcessInstanceEvent flow = createProcessInstance(PROCESS_ID, Map.of());

        // when
        throwErrorServiceTask("risk-level", 1);
        evaluateDecision("delayCalculator", 1);

        // then
        BpmnAssert.assertThat(flow)
                .hasPassedElement("Task_DelayCalculator")
                .isWaitingAtElements("delay");
    }
}
