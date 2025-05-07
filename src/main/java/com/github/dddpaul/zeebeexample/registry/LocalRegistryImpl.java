package com.github.dddpaul.zeebeexample.registry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(value = "app.registry.redis.enabled", havingValue = "false")
public class LocalRegistryImpl implements ProcessRegistry {

    private final ConcurrentHashMap<Long, Instant> processes = new ConcurrentHashMap<>();

    @Override
    public Map<Long, Instant> all() {
        return processes;
    }

    @Override
    public Instant get(long key) {
        return processes.get(key);
    }

    @Override
    public void put(long key, Instant timestamp) {
        processes.put(key, timestamp);
    }

    @Override
    public void remove(Long key) {
        processes.remove(key);
    }
}
