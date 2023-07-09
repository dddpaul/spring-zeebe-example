package com.github.dddpaul.zeebeexample;

import com.github.dddpaul.zeebeexample.starter.ProcessStarter;
import com.github.dddpaul.zeebeexample.workers.WorkerProgressBar;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:*.bpmn")
public class ZeebeExampleApplication implements ApplicationRunner {

    @Autowired(required = false)
    private ProcessStarter starter;

    @Value("${app.starter.enabled:true}")
    private boolean starterEnabled;

    @Autowired(required = false)
    private WorkerProgressBar worker;

    @Value("${app.worker.enabled:true}")
    private boolean workerEnabled;

    public static void main(String[] args) {
        SpringApplication.run(ZeebeExampleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        if (starterEnabled && starter != null) {
            starter.startParallelProcesses();
        }
        if (workerEnabled && worker != null) {
            worker.startProgressBar();
        }
    }
}
