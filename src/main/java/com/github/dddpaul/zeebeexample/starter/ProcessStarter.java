package com.github.dddpaul.zeebeexample.starter;

import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import com.github.dddpaul.zeebeexample.starter.commands.SendStartMessageCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
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
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty(value = "app.starter.enabled", havingValue = "true")
public class ProcessStarter {

    private static final Logger log = LoggerFactory.getLogger(ProcessStarter.class);

    @Autowired
    private ProcessStarterConfiguration config;

    @Autowired
    private SendStartMessageCommand command;

    public void startParallelProcesses() {
        ExecutorService pool = Executors.newFixedThreadPool(config.getThreads());

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

    private Runnable startProcesses(ProgressBar bar, long count) {
        return () -> {
            try (bar) {
                for (long i = 0; i < count; i++) {
                    PublishMessageResponse response = command.execute();
                    bar.setExtraMessage(String.format(" %s %17d", response.getTenantId(), response.getMessageKey()));
                    bar.step();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        };
    }
}
