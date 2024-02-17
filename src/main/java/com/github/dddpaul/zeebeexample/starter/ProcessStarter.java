package com.github.dddpaul.zeebeexample.starter;

import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty(value = "app.starter.enabled", havingValue = "true")
public class ProcessStarter {

    private static final Logger log = LoggerFactory.getLogger(ProcessStarter.class);

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private CreateInstanceCommand command;

    @Autowired
    private ApplicationStats stats;

    private final AtomicLong processCounter = new AtomicLong(0);

    public void startParallelProcesses() {
        try (ExecutorService pool = Executors.newFixedThreadPool(config.threads())) {

            List<ProgressBar> bars = IntStream.range(0, config.threads())
                    .mapToObj(i -> new ProgressBarBuilder()
                            .setTaskName(String.format("%10s", "Thread-" + i))
                            .setInitialMax(config.count())
                            .showSpeed()
                            .build())
                    .toList();

            CompletableFuture<?>[] futures = bars.stream()
                    .map(bar -> CompletableFuture.runAsync(startProcesses(bar, config.count()), pool))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            pool.shutdown();
        }
        if (processCounter.get() != config.count() * config.threads()) {
            throw new RuntimeException("Actual processes created: %d, expected: %d".formatted(processCounter.get(), config.count()));
        }
    }

    private Runnable startProcesses(ProgressBar bar, long count) {
        return () -> {
            try (bar) {
                for (long i = 0; i < count; i++) {
                    ProcessInstanceEvent event = command.execute(processCounter.incrementAndGet());
                    stats.incrementCreated();
                    bar.setExtraMessage(String.format(" %s %17d", event.getBpmnProcessId(), event.getProcessInstanceKey()));
                    bar.step();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        };
    }
}
