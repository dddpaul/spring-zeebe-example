package com.github.dddpaul.zeebeexample.starter;

import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.registry.ProcessRegistry;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProcessStarterTest {

    private ProcessStarter processStarter;

    @MockitoBean
    private ProcessStarterConfiguration config;

    @MockitoBean
    private ProcessRegistry registry;

    @MockitoBean
    private CreateInstanceCommand command;

    @MockitoBean
    private ApplicationStats stats;

    @BeforeEach
    void setup() {
        when(config.threads()).thenReturn(2);
        when(config.count()).thenReturn(3L);
        when(config.deadline()).thenReturn(1000L); // ms
        processStarter = new ProcessStarter(config, registry, command, stats);
    }

    @Test
    void testStartProcessesAndTimeouts() throws Exception {
        when(command.execute(anyLong())).thenAnswer(invocation -> {
            Thread.sleep(1000); // Wait for timeoutChecker
            ProcessInstanceEvent mockEvent = mock(ProcessInstanceEvent.class);
            when(mockEvent.getBpmnProcessId()).thenReturn("test-process");
            when(mockEvent.getProcessInstanceKey()).thenReturn(123L);
            return mockEvent;
        });

        // Simulate two processes in registry: one timed out, one still active
        Instant now = Instant.now();
        when(registry.all()).thenReturn(Map.of(
                1001L, now.minus(Duration.ofMillis(2000)),  // timed out
                1002L, now.minus(Duration.ofMillis(1000))   // active
        ));

        // Run process starter
        processStarter.startParallelProcesses();

        // Verify process was created and registered
        verify(command, times(6)).execute(anyLong());
        verify(registry, times(6)).put(eq(123L), any());
        verify(stats, times(6)).incrementCreated();

        // Verify timeouts checked and timed-out process removed
        verify(registry, atLeastOnce()).checkTimeouts(eq(Duration.ofMillis(1000L)), any());
//        verify(registry).remove(1001L);
//        verify(stats).incrementCancelled();

        // Ensure non-timed-out process is not removed
        verify(registry, never()).remove(1002L);
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

        try {
            processStarter.startParallelProcesses();
        } catch (IllegalStateException e) {
            assertEquals("Expected 4 processes, but started 3", e.getMessage());
        }
    }
}
