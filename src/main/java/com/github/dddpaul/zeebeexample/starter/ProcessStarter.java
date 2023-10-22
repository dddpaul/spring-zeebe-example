package com.github.dddpaul.zeebeexample.starter;

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

    private final AtomicLong processCounter = new AtomicLong(0);

    public void startParallelProcesses() {
        try (ExecutorService pool = Executors.newFixedThreadPool(config.getThreads())) {

            List<ProgressBar> bars = IntStream.range(0, config.getThreads())
                    .mapToObj(i -> new ProgressBarBuilder()
                            .setTaskName(String.format("%10s", "Thread-" + i))
                            .setInitialMax(config.getCount())
                            .showSpeed()
                            .build())
                    .toList();

            CompletableFuture<?>[] futures = bars.stream()
                    .map(bar -> CompletableFuture.runAsync(startProcesses(bar, config.getCount()), pool))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            pool.shutdown();
        }
    }

    private Runnable startProcesses(ProgressBar bar, long count) {
        return () -> {
            try (bar) {
                for (long i = 0; i < count; i++) {
                    ProcessInstanceEvent event = command.execute(processCounter.incrementAndGet());
                    bar.setExtraMessage(String.format(" %s %17d", event.getBpmnProcessId(), event.getProcessInstanceKey()));
                    bar.step();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        };
    }
}
