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
        when(config.deadline()).thenReturn(100L); // ms
        processStarter = new ProcessStarter(config, registry, command, stats);
    }

    @Test
    void testProperProcessCount() throws Exception {
        // given
        when(command.execute(anyLong())).thenAnswer(invocation -> {
            ProcessInstanceEvent mockEvent = mock(ProcessInstanceEvent.class);
            when(mockEvent.getBpmnProcessId()).thenReturn("test-process");
            when(mockEvent.getProcessInstanceKey()).thenReturn(123L);
            return mockEvent;
        });

        // when
        processStarter.startParallelProcesses();

        // then
        verify(command, times(6)).execute(anyLong());
        verify(registry, times(6)).put(eq(123L), any());
        verify(stats, times(6)).incrementCreated();
    }

    @Test
    void testTimeoutCheckerInvoked() throws Exception {
        // given
        when(command.execute(anyLong())).thenAnswer(invocation -> {
            Thread.sleep(100); // Wait for timeoutChecker
            return mock(ProcessInstanceEvent.class);
        });

        // when
        processStarter.startParallelProcesses();

        // then
        verify(registry, atLeastOnce()).checkTimeouts(eq(Duration.ofMillis(100L)), any());
    }

    @Test
    void testProcessCountMismatch() throws Exception {
        // given
        when(command.execute(anyLong())).thenReturn(mock(ProcessInstanceEvent.class));
        when(config.count()).thenReturn(2L); // Expect 4 processes (2 threads * 2 count)

        try {
            // when
            processStarter.startParallelProcesses();
        } catch (IllegalStateException e) {
            // then
            assertEquals("Expected 4 processes, but started 3", e.getMessage());
        }
    }
}
