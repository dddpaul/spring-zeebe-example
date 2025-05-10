package com.github.dddpaul.zeebeexample.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalRegistryTest {

    private ProcessRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new LocalRegistryImpl();
    }

    @Test
    void shouldRemoveExpiredAndRunCallback() throws Exception {
        // given
        CountDownLatch latch = new CountDownLatch(1);
        registry.setExpiration(Duration.ofMillis(100), latch::countDown);
        long key = 123L;
        registry.put(key, Instant.now());

        // when
        boolean triggered = latch.await(1000, TimeUnit.MILLISECONDS);

        // then
        assertNull(registry.get(key), "Expired process was not removed");
        assertTrue(triggered, "Timeout callback was not triggered");
    }

    @Test
    void shouldNotInvokeCallbackForEmptyRegistry(@Mock Runnable callback) {
        registry.setExpiration(Duration.ofMinutes(1), callback);
        verifyNoInteractions(callback);
    }
}
