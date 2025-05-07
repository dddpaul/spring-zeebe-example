package com.github.dddpaul.zeebeexample.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessRegistryTest {

    private ProcessRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new LocalRegistryImpl();
    }

    @Test
    void shouldCheckTimeoutsAndRemoveExpired(@Mock Runnable onTimeout) {
        Instant oldTime = Instant.now().minus(Duration.ofMinutes(5));
        Instant recentTime = Instant.now().minus(Duration.ofSeconds(30));

        registry.put(1L, oldTime);
        registry.put(2L, recentTime);

        registry.checkTimeouts(Duration.ofMinutes(1), onTimeout);

        verify(onTimeout, times(1)).run();
        assertNull(registry.get(1L));
        assertNotNull(registry.get(2L));
    }

    @Test
    void shouldNotCallTimeoutForEmptyRegistry(@Mock Runnable onTimeout) {
        registry.checkTimeouts(Duration.ofMinutes(1), onTimeout);
        verifyNoInteractions(onTimeout);
    }
}
