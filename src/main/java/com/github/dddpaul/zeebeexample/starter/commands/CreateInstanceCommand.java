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

import java.util.Map;
import java.util.Random;

@Component
public class CreateInstanceCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateInstanceCommand.class);

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private ZeebeClient client;

    private final ObjectMapper mapper = new ObjectMapper();

    public ProcessInstanceEvent execute(long counter) throws JsonProcessingException, InterruptedException {
        Map<String, String> variables = config.getVariables();
        variables.put("count", String.valueOf(counter));
        if (config.isRandom()) {
            Random random = new Random();
            variables.put("chance", String.valueOf(random.nextInt(RiskLevel.values().length + 1)));
        }
        String s = mapper.writeValueAsString(variables);
        for (int retries = 1; retries <= 10; retries++) {
            try {
                ProcessInstanceEvent event = client
                        .newCreateInstanceCommand()
                        .bpmnProcessId(config.getProcess())
                        .latestVersion()
                        .variables(s)
                        .send()
                        .join();
                log.debug("Application {} sent with variables: {}", event.getProcessInstanceKey(), s);
                return event;
            } catch (ClientStatusException e) {
                // Deadline is for handling broker flapping, Internal is for handling rebalance
                if (Status.DEADLINE_EXCEEDED.equals(e.getStatus()) || (Status.INTERNAL.equals(e.getStatus()))) {
                    Thread.sleep(1000);
                }
            }
        }
        throw new RuntimeException("Process create error: All retries are failed");
    }
}
