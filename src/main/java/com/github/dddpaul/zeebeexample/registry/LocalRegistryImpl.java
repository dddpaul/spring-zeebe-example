package com.github.dddpaul.zeebeexample.registry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(value = "app.registry.redis.enabled", havingValue = "false")
public class LocalRegistryImpl implements ProcessRegistry {

    private final ConcurrentHashMap<Long, Instant> processes = new ConcurrentHashMap<>();

    @Override
    public Instant get(long key) {
        return processes.get(key);
    }

    @Override
    public void put(long key, Instant timestamp) {
        processes.put(key, timestamp);
    }

    @Override
    public void setExpiration(Duration timeout, Runnable callback) {
        Instant now = Instant.now();
        processes.forEach((key, start) -> {
            if (start != null && Duration.between(start, now).compareTo(timeout) > 0) {
                processes.remove(key);
                callback.run();
            }
        });
    }
}
