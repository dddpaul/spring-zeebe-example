package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath:demoProcess.bpmn")
public class ZeebeExampleApplication implements ApplicationRunner {

    @Autowired(required = false)
    private ProcessStarter starter;

    @Value("${app.starter.enabled:true}")
    private boolean starterEnabled;

    public static void main(String[] args) {
        SpringApplication.run(ZeebeExampleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (starterEnabled && starter != null) {
            starter.startProcesses();
        }
    }
}
