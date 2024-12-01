package com.github.dddpaul.zeebeexample.starter.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dddpaul.zeebeexample.RiskLevel;
import com.github.dddpaul.zeebeexample.starter.ProcessStarterConfiguration;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ClientStatusException;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.grpc.Status.*;

@Component
public class CreateInstanceCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateInstanceCommand.class);

    // Deadline is for handling broker flapping, Internal is for handling rebalance, ResourceExhausted/Unavailable is for handling flow control
    private static final List<Status.Code> TEMPORAL_ERRORS = List.of(
            DEADLINE_EXCEEDED.getCode(),
            INTERNAL.getCode(),
            RESOURCE_EXHAUSTED.getCode(),
            UNAVAILABLE.getCode());

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private ZeebeClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    public ProcessInstanceEvent execute(long counter) throws JsonProcessingException, InterruptedException {
        Map<String, String> variables = config.variables();
        variables.put("count", String.valueOf(counter));
        if (config.random()) {
            Random random = new Random();
            variables.put("chance", String.valueOf(random.nextInt(RiskLevel.values().length + 1)));
        }
        String s = mapper.writeValueAsString(variables);
        for (int retries = 1; retries <= 10; retries++) {
            try {
                ProcessInstanceEvent event = client
                        .newCreateInstanceCommand()
                        .bpmnProcessId(config.process())
                        .latestVersion()
                        .variables(s)
                        .send()
                        .join();
                log.debug("Application {} sent with variables: {}", event.getProcessInstanceKey(), s);
                return event;
            } catch (ClientStatusException e) {
                if (TEMPORAL_ERRORS.contains(e.getStatus().getCode())) {
                    Thread.sleep(retries * 1000);
                } else {
                    log.error("Permanent error for application: count={}, retry={}", counter, retries, e);
                }
            }
        }
        throw new RuntimeException("Process create error: All retries are failed");
    }
}
