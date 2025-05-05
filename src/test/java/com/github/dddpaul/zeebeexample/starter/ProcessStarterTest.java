package com.github.dddpaul.zeebeexample.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.registry.ProcessRegistry;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProcessStarterTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProcessStarter processStarter() {
            return new ProcessStarter();
        }
    }

    @Autowired
    private ProcessStarter processStarter;

    @MockitoBean
    private CreateInstanceCommand command;

    @MockitoBean
    private ApplicationStats stats;

    @MockitoBean
    private ProcessRegistry registry;

    @MockitoBean
    private ProcessStarterConfiguration config;

    @BeforeEach
    void setupMocks() {
        when(config.threads()).thenReturn(2);
        when(config.count()).thenReturn(3L);
        when(config.deadline()).thenReturn(100L); // ms
    }

    @Test
    void testStartProcessesAndTimeouts() throws InterruptedException, JsonProcessingException {
        AtomicInteger counter = new AtomicInteger();
        when(command.execute(anyLong())).thenAnswer(invocation -> {
            long processKey = counter.incrementAndGet();
            ProcessInstanceEvent mockEvent = mock(ProcessInstanceEvent.class);
            when(mockEvent.getBpmnProcessId()).thenReturn("test-process");
            when(mockEvent.getProcessInstanceKey()).thenReturn(processKey);
            return mockEvent;
        });

        // simulate registry containing timed out processes
        when(registry.all()).thenReturn(Map.of(1001L, Instant.now()));

        processStarter.startParallelProcesses();

        // Wait for timeout checker
        Thread.sleep(1000);

        // Verify 6 total processes created (2 threads * 3 count)
        verify(command, times(6)).execute(anyLong());
        verify(registry, times(6)).put(anyLong(), any());
        verify(stats, times(6)).incrementCreated();

        // Verify timeout detection
        verify(registry).remove(1001L);
        verify(stats).incrementCancelled();
    }
}
