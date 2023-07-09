package com.github.dddpaul.zeebeexample.workers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "app.worker.enabled", havingValue = "true")
public class WorkerProgressBar {

    private static final Logger log = LoggerFactory.getLogger(WorkerProgressBar.class);

    @Autowired
    private MeterRegistry registry;

    public void startProgressBar() throws InterruptedException {
        int i = 1;
        while (true) {
            try (ProgressBar activatedBar = new ProgressBarBuilder()
                    .setTaskName("Activated")
                    .showSpeed()
                    .setInitialMax(10000)
                    .build();
                 ProgressBar completedBar = new ProgressBarBuilder()
                         .setTaskName("Completed")
                         .showSpeed()
                         .setInitialMax(10000)
                         .build()) {
                while (true) {
                    try {
                        Counter activatedCounter = registry.get("camunda.job.invocations")
                                .tags("action", "activated")
                                .counter();
                        Counter completedCounter = registry.get("camunda.job.invocations")
                                .tags("action", "completed")
                                .counter();
                        long a = Math.round(activatedCounter.count());
                        long c = Math.round(completedCounter.count());
                        if (a > i * activatedBar.getMax()) {
                            activatedBar.stepTo(activatedBar.getMax() + a % activatedBar.getMax());
                            completedBar.stepTo(activatedBar.getMax() + c % completedBar.getMax());
                            break;
                        }
                        activatedBar.stepTo(a % activatedBar.getMax());
                        completedBar.stepTo(c % completedBar.getMax());
                        Thread.sleep(500);
                    } catch (MeterNotFoundException e) {
                        Thread.sleep(500);
                    }
                }
            }
            i++;
        }
    }
}
