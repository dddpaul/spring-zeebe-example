package com.github.dddpaul.zeebeexample.starter;

import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.registry.ProcessRegistry;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.runAsync;

@Component
@ConditionalOnProperty(value = "app.starter.enabled", havingValue = "true")
public class ProcessStarter {

    private static final Logger log = LoggerFactory.getLogger(ProcessStarter.class);

    public ProcessStarter(ProcessStarterConfiguration config, ProcessRegistry registry, CreateInstanceCommand command, ApplicationStats stats) {
        this.config = config;
        this.registry = registry;
        this.command = command;
        this.stats = stats;
    }

    @Autowired
    private ProcessStarterConfiguration config;
    @Autowired
    private ProcessRegistry registry;
    @Autowired
    private CreateInstanceCommand command;
    @Autowired
    private ApplicationStats stats;

    private final AtomicLong processCounter = new AtomicLong();

    public void startParallelProcesses() {
        registry.setExpiration(Duration.ofMillis(config.timeout()), stats::incrementCancelled);
        try (ExecutorService pool = Executors.newFixedThreadPool(config.threads())) {
            List<ProgressBar> bars = IntStream.range(0, config.threads())
                    .mapToObj(i -> new ProgressBarBuilder()
                            .setTaskName(String.format("%10s", "Thread-" + i))
                            .setInitialMax(config.count())
                            .showSpeed()
                            .build())
                    .toList();

            CompletableFuture<?>[] futures = bars.stream()
                    .map(bar -> runAsync(() -> startProcesses(bar, config.count()), pool))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();

            if (processCounter.get() != config.count() * config.threads()) {
                throw new IllegalStateException("Expected %d processes, but started %d".formatted(
                        config.count() * config.threads(), processCounter.get()));
            }
        } catch (Exception e) {
            log.error("Error while starting parallel processes", e);
            throw new RuntimeException(e);
        }
    }

    private void startProcesses(ProgressBar bar, long count) {
        try (bar) {
            for (long i = 0; i < count; i++) {
                long currentCount = processCounter.incrementAndGet();
                ProcessInstanceEvent event = command.execute(currentCount);
                stats.incrementCreated();
                registry.put(event.getProcessInstanceKey(), Instant.now());
                bar.setExtraMessage(String.format(" %s %17d", event.getBpmnProcessId(), event.getProcessInstanceKey()));
                bar.step();
            }
        } catch (Exception e) {
            log.error("Error while starting process", e);
        }
    }
}
