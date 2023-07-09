package com.github.dddpaul.zeebeexample.starter.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dddpaul.zeebeexample.starter.ProcessStarterConfiguration;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateInstanceCommand {

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private ZeebeClient client;

    public ProcessInstanceEvent execute() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return client
                .newCreateInstanceCommand()
                .bpmnProcessId(config.getProcess())
                .latestVersion()
                .variables(objectMapper.writeValueAsString(config.getVariables()))
                .send()
                .join();
    }
}
