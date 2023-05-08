package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.UUID;

@SpringBootTest
class ZeebeExampleApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(ZeebeExampleApplicationTests.class);

	@Autowired
	private ZeebeClient client;

	@Test
	void contextLoads() {
	}

	@RepeatedTest(100000)
	void testTasks() throws InterruptedException {
		final ProcessInstanceEvent event = client
						.newCreateInstanceCommand()
						.bpmnProcessId("demoProcess")
						.latestVersion()
						.variables("{\"a\": \"" + UUID.randomUUID().toString() + "\",\"b\": \"" + new Date().toString() + "\"}")
						.send()
						.join();

//		log.info("started instance for workflowKey='{}', bpmnProcessId='{}', version='{}' with workflowInstanceKey='{}'",
//				event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
	}
}
