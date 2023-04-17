package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableZeebeClient
@Deployment(resources = "classpath:demoProcess.bpmn")
public class ZeebeExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZeebeExampleApplication.class, args);
	}

}
