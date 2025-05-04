package com.github.dddpaul.zeebeexample.starter;

import com.github.dddpaul.zeebeexample.actuator.ApplicationStats;
import com.github.dddpaul.zeebeexample.starter.commands.CreateInstanceCommand;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessStarterTest {

    @Mock
    private ProcessStarterConfiguration config;

    @Mock
    private CreateInstanceCommand command;

    @Mock
    private ApplicationStats stats;

    @Mock
    private ProcessInstanceEvent processInstanceEvent;

    @InjectMocks
    private ProcessStarter processStarter;

    @BeforeEach
    void setUp() {
        when(config.threads()).thenReturn(2);
        when(config.count()).thenReturn(3L);
        when(config.deadline()).thenReturn(1000L);
        when(processInstanceEvent.getProcessInstanceKey()).thenReturn(1L);
        when(processInstanceEvent.getBpmnProcessId()).thenReturn("test-process");
    }

    @Test
    void shouldStartProcessesInParallel() throws Exception {
        when(command.execute(anyLong())).thenReturn(processInstanceEvent);

        processStarter.startParallelProcesses();

        verify(command, times(6)).execute(anyLong());
        verify(stats, times(6)).incrementCreated();
    }

//    @Test
//    void shouldHandleProcessTimeouts() {
//        ProcessStarter starter = new ProcessStarter();
//        starter.processesTimes = mock(Map.class);
//        when(starter.processesTimes.entrySet()).thenReturn(
//                Map.of(1L, Instant.now().minus(Duration.ofSeconds(2)))
//        ).thenReturn(Map.of());
//
//        starter.checkTimeouts(Duration.ofSeconds(1));
//
//        verify(stats).incrementCancelled();
//        verify(starter.processesTimes).remove(1L);
//    }

//    @Test
//    void shouldTrackProcessCountCorrectly() throws Exception {
//        when(command.execute(anyLong())).thenReturn(processInstanceEvent);
//
//        processStarter.startParallelProcesses();
//
//        assertEquals(6, processStarter.processCounter.get());
//    }

//    @Test
//    void shouldHandleCommandExecutionFailure() throws Exception {
//        when(command.execute(anyLong()))
//            .thenReturn(processInstanceEvent)
//            .thenThrow(new RuntimeException("Test exception"));
//
//        assertDoesNotThrow(() -> processStarter.startParallelProcesses());
//    }
}
