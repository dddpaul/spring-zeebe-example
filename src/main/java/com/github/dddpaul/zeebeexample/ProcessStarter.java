package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@ConditionalOnProperty(value = "app.starter.enabled", havingValue = "true")
public class ProcessStarter {

    private static final Logger log = LoggerFactory.getLogger(ProcessStarter.class);

    @Value("${app.starter.count}")
    private long count = 0;

    @Autowired
    private ZeebeClient client;

    public void startProcesses() {
        for (long i = 0; i < count; i++) {
            final ProcessInstanceEvent event = client
                    .newCreateInstanceCommand()
                    .bpmnProcessId("demoProcess")
                    .latestVersion()
                    .variables("{\"a\": \"" + UUID.randomUUID().toString() + "\",\"b\": \"" + new Date().toString() + "\"}")
                    .send()
                    .join();
            log.info("started instance for workflowKey='{}', bpmnProcessId='{}', version='{}' with workflowInstanceKey='{}'",
                    event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
        }
    }
}
