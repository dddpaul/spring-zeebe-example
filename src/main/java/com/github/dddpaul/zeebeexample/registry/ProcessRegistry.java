package com.github.dddpaul.zeebeexample.registry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public interface ProcessRegistry {
    Map<Long, Instant> all();
    Instant get(long key);
    void put(long key, Instant timestamp);
    void remove(Long key);

    default void checkTimeouts(Duration deadline, Runnable onTimeout) {
        Instant now = Instant.now();
        all().forEach((key, start) -> {
            if (start != null && Duration.between(start, now).compareTo(deadline) > 0) {
                remove(key);
                onTimeout.run();
            }
        });
    }
}
