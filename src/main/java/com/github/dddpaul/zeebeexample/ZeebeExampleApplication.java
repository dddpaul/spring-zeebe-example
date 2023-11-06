package com.github.dddpaul.zeebeexample;

import com.github.dddpaul.zeebeexample.starter.ProcessStarter;
import com.github.dddpaul.zeebeexample.workers.WorkerProgressBar;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.jobhandling.ZeebeClientExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
@Deployment(resources = {"classpath*:*.bpmn", "classpath*:*.dmn"})
public class ZeebeExampleApplication implements ApplicationRunner {

    @Bean
    public ZeebeClientExecutorService zeebeClientExecutorService() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory());
        return new ZeebeClientExecutorService(pool);
    }
    
    @Autowired(required = false)
    private ProcessStarter starter;

    @Value("${app.starter.enabled:true}")
    private boolean starterEnabled;

    @Autowired(required = false)
    private WorkerProgressBar progressBar;

    @Value("${app.worker.progress-bar.enabled:true}")
    private boolean workerProgressBarEnabled;

    public static void main(String[] args) {
        SpringApplication.run(ZeebeExampleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        if (starterEnabled && starter != null) {
            starter.startParallelProcesses();
        }
        if (workerProgressBarEnabled && progressBar != null) {
            progressBar.start();
        }
    }
}
