package com.github.dddpaul.zeebeexample;

import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.worker.enabled", havingValue = "true")
public class ExampleJobWorkers {

  private static final Logger log = LoggerFactory.getLogger(ExampleJobWorkers.class);

  @JobWorker(type = "foo") // autoComplete = true as default value
  public void handleFooJob(final ActivatedJob job) {
    logJob(job, null);
  }

  @JobWorker() // type is set to method name: "bar".
  public Map<String, Object> bar(final ActivatedJob job, @Variable String a) throws InterruptedException {
//    Thread.sleep(5000);
    logJob(job, a);
    return Collections.singletonMap("someResult", "42");
  }

  @JobWorker(type = "fail", fetchAllVariables = true)
  public void handleFailingJob(final JobClient client, final ActivatedJob job, @Variable String someResult) {
    logJob(job, someResult);
    throw new ZeebeBpmnError("DOESNT_WORK", "This will actually never work :-)");
  }

  private static void logJob(final ActivatedJob job, Object parameterValue) {
  }

}
