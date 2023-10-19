package com.github.dddpaul.zeebeexample.starter.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dddpaul.zeebeexample.RiskLevel;
import com.github.dddpaul.zeebeexample.starter.ProcessStarterConfiguration;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class CreateInstanceCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateInstanceCommand.class);

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private ZeebeClient client;

    public ProcessInstanceEvent execute() throws JsonProcessingException, InterruptedException {
        Map<String, String> variables = config.getVariables();
        if (config.isRandom()) {
            Random random = new Random();
            variables.put("chance", String.valueOf(random.nextInt(RiskLevel.values().length + 1)));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        for (int retries = 1; retries <= 3; retries++) {
            try {
                ProcessInstanceEvent event = client
                        .newCreateInstanceCommand()
                        .bpmnProcessId(config.getProcess())
                        .latestVersion()
                        .variables(objectMapper.writeValueAsString(variables))
                        .send()
                        .join();
                log.debug("Application {} sent with chance = {}", event.getProcessInstanceKey(), variables.get("chance"));
                return event;
            } catch (StatusRuntimeException e) {
                if (Status.DEADLINE_EXCEEDED.equals(e.getStatus())) {
                    Thread.sleep(1000);
                }
            }
        }
        throw new RuntimeException("Process create error: All retries are failed");
    }
}
