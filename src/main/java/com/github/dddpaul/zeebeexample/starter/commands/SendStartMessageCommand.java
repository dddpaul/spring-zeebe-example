package com.github.dddpaul.zeebeexample.starter.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dddpaul.zeebeexample.RiskLevel;
import com.github.dddpaul.zeebeexample.starter.ProcessStarterConfiguration;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class SendStartMessageCommand {

    private static final Logger log = LoggerFactory.getLogger(SendStartMessageCommand.class);

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private ZeebeClient client;

    public PublishMessageResponse execute() throws JsonProcessingException {
        Map<String, String> variables = config.variables();
        if (config.random()) {
            Random random = new Random();
            variables.put("chance", String.valueOf(random.nextInt(RiskLevel.values().length + 1)));
        }
        ObjectMapper objectMapper = new ObjectMapper();
        PublishMessageResponse response = client
                .newPublishMessageCommand()
                .messageName(config.messages().get("start"))
                .correlationKey("")
                .variables(objectMapper.writeValueAsString(variables))
                .send()
                .join();

        log.debug("Application {} sent with chance = {}", response.getMessageKey(), variables.get("chance"));
        return response;
    }
}
