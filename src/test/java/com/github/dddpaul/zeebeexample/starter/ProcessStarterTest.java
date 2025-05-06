package com.github.dddpaul.zeebeexample.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.registry.ProcessRegistry;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void testStartProcessesAndTimeouts() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        when(command.execute(anyLong())).thenAnswer(invocation -> {
            long processKey = counter.incrementAndGet();
            ProcessInstanceEvent mockEvent = mock(ProcessInstanceEvent.class);
            when(mockEvent.getBpmnProcessId()).thenReturn("test-process");
            when(mockEvent.getProcessInstanceKey()).thenReturn(processKey);
            return mockEvent;
        });

        // Mock registry with one timed out process
        when(registry.all()).thenReturn(Map.of(
            1001L, Instant.now().minus(Duration.ofMillis(200)), // timed out
            1002L, Instant.now().minus(Duration.ofMillis(50))  // not timed out
        ));

        processStarter.startParallelProcesses();

        // Verify process creation
        verify(command, times(6)).execute(anyLong());
        verify(registry, times(6)).put(anyLong(), any());
        verify(stats, times(6)).incrementCreated();

        // Verify timeout checking was scheduled
        ArgumentCaptor<Runnable> timeoutCheckerCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(registry).checkTimeouts(eq(Duration.ofMillis(100)), timeoutCheckerCaptor.capture());
        
        // Verify timeout handling
        timeoutCheckerCaptor.getValue().run();
        verify(registry).remove(1001L);
        verify(stats).incrementCancelled();
        verify(registry, never()).remove(1002L); // Should not remove non-timed out process
    }

    @Test
    void testExecutorShutdown() throws Exception {
        when(command.execute(anyLong())).thenReturn(mock(ProcessInstanceEvent.class));
        
        processStarter.startParallelProcesses();

        // Verify no active threads remain
        assertEquals(0, Thread.activeCount() - Thread.currentThread().getThreadGroup().activeCount());
    }

    @Test
    void testProcessCountMismatch() throws Exception {
        when(command.execute(anyLong())).thenReturn(mock(ProcessInstanceEvent.class));
        when(config.count()).thenReturn(2L); // Expect 4 processes (2 threads * 2 count)
        
        // Simulate one process failing to start
        doThrow(new RuntimeException("Failed")).when(command).execute(eq(4L));

        try {
            processStarter.startParallelProcesses();
        } catch (IllegalStateException e) {
            assertEquals("Expected 4 processes, but started 3", e.getMessage());
        }
    }
}
